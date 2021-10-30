package com.istiaksaif.medops.Utils;

import java.util.Calendar;

public class AgeCalculator {
    private int startYear, startMonth, startDay, endYear, endMonth, endDay, resYear, resMonth, resDay;
    private Calendar start, end;
    public String getCurrentDate()
    {
        end=Calendar.getInstance();
        endYear=end.get(Calendar.YEAR);
        endMonth=end.get(Calendar.MONTH);
        endMonth++;
        endDay=end.get(Calendar.DAY_OF_MONTH);
        return endDay+":"+endMonth+":"+endYear;
    }
    public void setDateOfBirth(int sYear, int sMonth, int sDay)
    {
        startYear=sYear;
        startMonth=sMonth;
        startMonth++;
        startDay=sDay;

    }
    public void calcualteYear()
    {
        resYear=endYear-startYear;

    }

    public void calcualteMonth()
    {
        if(endMonth>=startMonth)
        {
            resMonth= endMonth-startMonth;
        }
        else
        {
            resMonth=endMonth-startMonth;
            resMonth=12+resMonth;
            resYear--;
        }

    }
    public void  calcualteDay()
    {

        if(endDay>=startDay)
        {
            resDay= endDay-startDay;
        }
        else
        {
            resDay=endDay-startDay;
            resDay=30+resDay;
            if(resMonth==0)
            {
                resMonth=11;
                resYear--;
            }
            else
            {
                resMonth--;
            }

        }
    }

    public String getResult()
    {
        return resYear + " yrs";
    }
    public long getSecond()
    {
        start=Calendar.getInstance();
        start.set(Calendar.YEAR, startYear);
        start.set(Calendar.MONTH, startMonth);
        start.set(Calendar.DAY_OF_MONTH, startDay);
        start.set(Calendar.HOUR, 12);
        start.set(Calendar.MINUTE, 30);
        start.set(Calendar.SECOND, 30);
        start.set(Calendar.MILLISECOND, 30);
        long now=end.getTimeInMillis();
        long old=start.getTimeInMillis();
        long diff=old-now;
        return diff/1000;
    }
}