package com.istiaksaif.medops.Fragment;

import static android.app.Activity.RESULT_OK;

import static com.airbnb.lottie.L.TAG;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.mlkit.common.model.LocalModel;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class UserHomeFragment extends Fragment {

    private ImageGetHelper getImageFunction;
    private LinearLayout takeImageCard;
    private TextView takeImageButton,predictButton,predictResult;
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

//    private FirebaseAutoMLRemoteModel mlRemoteModel = new FirebaseAutoMLRemoteModel.Builder("your_remote_model").build();
    private LocalModel localModel =
        new LocalModel.Builder()
                .setAssetManifestFilePath("your_remote_model")
                .build();
    private FirebaseModelDownloadConditions modelDownloadConditions = new FirebaseModelDownloadConditions.Builder()
            .requireWifi().build();
    private CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
            .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
            .build();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getImageFunction = new ImageGetHelper(this,null);

        takeImageCard = view.findViewById(R.id.takeimgcard);
        takeImageButton = view.findViewById(R.id.takeimgbutton);

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
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getImageFunction.IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            Uri image = data.getData();
            predictImg.setImageURI(image);
            predictButton.setVisibility(View.VISIBLE);
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
//        interpreter.close();
    }
    private void checkModel(Uri uri) {

        FirebaseModelDownloader.getInstance()
                .getModel("MedOps_Covid_Detection", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            Interpreter interpreter = new Interpreter(modelFile);
                            Bitmap bitmap = null;
                            try {
                                bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri), 224, 224, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
                            for (int y = 0; y < 224; y++) {
                                for (int x = 0; x < 224; x++) {
                                    int px = bitmap.getPixel(x, y);

                                    int r = Color.red(px);
                                    int g = Color.green(px);
                                    int b = Color.blue(px);

                                    float rf = (r - 127) / 255.0f;
                                    float gf = (g - 127) / 255.0f;
                                    float bf = (b - 127) / 255.0f;

                                    input.putFloat(rf);
                                    input.putFloat(gf);
                                    input.putFloat(bf);
                                }
                            }
                            int bufferSize = 1000 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
                            ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
                            interpreter.run(input, modelOutput);
                            modelOutput.rewind();
                            FloatBuffer probabilities = modelOutput.asFloatBuffer();
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(getContext().getAssets().open("labels.txt")));
                                for (int i = 0; i < probabilities.capacity(); i++) {
                                    String label = reader.readLine();
                                    float probability = probabilities.get(i);
                                    Log.i(TAG, String.format("%s: %1.4f", label, probability));
                                    Toast.makeText(getContext(),String.format("%s: %1.4f", label, probability), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {

                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_home, container, false);
        return view;
    }
}