package com.istiaksaif.medops.Fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.istiaksaif.medops.R;
import com.istiaksaif.medops.Utils.ImageGetHelper;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserHomeFragment extends Fragment {

    private ImageGetHelper getImageFunction;
    private LinearLayout takeImageCard;
    private TextView predictButton,predictResult;
    private Bitmap img;
    private ImageView predictImg;

    protected Interpreter interpreter;
    private TensorImage inputImageBuffer;
    private TensorBuffer outputBuffer;
    private TensorProcessor tensorProcessor;
    private int imageSizeX,imageSizeY;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD= 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    private List<String> labels;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getImageFunction = new ImageGetHelper(this,null);

        takeImageCard = view.findViewById(R.id.takeimgcard);

        takeImageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFunction.pickFromGallery();
            }
        });

        predictResult = view.findViewById(R.id.predictResult);
        predictImg = view.findViewById(R.id.predictimg);

        predictButton = view.findViewById(R.id.predictbutton);

        try {
            interpreter = new Interpreter(loadTFLiteModel());
        } catch (IOException e) {
            e.printStackTrace();
        }
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoDetection();
            }
        });
    }
    private void autoDetection(){
        int imageTensorIndex = 0;
        int[] imageShape = interpreter.getInputTensor(imageTensorIndex).shape();
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = interpreter.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape = interpreter.getOutputTensor(probabilityTensorIndex).shape();
        DataType probabilityDataType = interpreter.getOutputTensor(probabilityTensorIndex).dataType();
        inputImageBuffer = new TensorImage(imageDataType);
        outputBuffer = TensorBuffer.createFixedSize(probabilityShape,probabilityDataType);
        tensorProcessor = new TensorProcessor.Builder().add(getPostProcessorNormalizeOP()).build();

        inputImageBuffer = loadImage(img);
        interpreter.run(inputImageBuffer.getBuffer(),outputBuffer.getBuffer().rewind());
        showResult();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getImageFunction.IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            Uri image = data.getData();
            predictImg.setImageURI(image);
            predictButton.setVisibility(View.VISIBLE);
            predictResult.setText("loading.........");
            try {
                img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private TensorImage loadImage(final Bitmap bitmap){
        inputImageBuffer.load(bitmap);
        int cropSize = Math.min(bitmap.getWidth(),bitmap.getHeight());
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize,cropSize))
                .add(new ResizeOp(imageSizeX,imageSizeY,ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(getPreProcessorNormalizeOP()).build();
        return imageProcessor.process(inputImageBuffer);
    }
    //load tfLite model
    private MappedByteBuffer loadTFLiteModel() throws IOException{
        AssetFileDescriptor fileDescriptor = getActivity().getAssets().openFd("MedOps.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declareLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    private TensorOperator getPreProcessorNormalizeOP(){
        return new NormalizeOp(IMAGE_MEAN,IMAGE_STD);
    }
    private TensorOperator getPostProcessorNormalizeOP(){
        return new NormalizeOp(PROBABILITY_MEAN,PROBABILITY_STD);
    }

    private void showResult(){
        try {
            labels = FileUtil.loadLabels(getActivity(),"labels.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,Float> labelProbability = new TensorLabel(labels,
                tensorProcessor.process(outputBuffer)).getMapWithFloatValue();
        float maxValueInMap = (Collections.max(labelProbability.values()));
        for(Map.Entry<String, Float> entry : labelProbability.entrySet()){
            if(entry.getValue()==maxValueInMap){
                predictResult.setText(entry.getKey());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_home, container, false);
        return view;
    }
}