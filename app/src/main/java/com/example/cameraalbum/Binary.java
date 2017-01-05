package com.example.cameraalbum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;


/**
 * Created by Freedom.Ly on 2017-1-3 08:46
 * Email  Freedom.JFL@Live.com
 */

class Binary {
    private static final String TAG = "Binary";

   public static Bitmap Binarization(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;        // 灰度平均值
        int graySum = 0;
        int grayMean = 0;
        int grayFrontMean = 0;
        int grayBackMean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) {        // 不计算边界行和列，为避免越界
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = (pix[x]) & 0xff;
//                pixelGray = (int) (0.3 * r + 0.59 * b + 0.11 * b);       // 计算每个坐标点的灰度
                pixelGray = (int) (0.33 * r + 0.33 * g + 0.33 *b);
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graySum += pixelGray;
            }
        }
        grayMean = (int) (graySum / area);      // 整个图的灰度平均
        average = grayMean;
        Log.i(TAG, "Average: " + average);
        for (int i = 0; i < width; i++) {     // 计算整个图的二值化阈值
            for (int j = 0; j < height; j++) {
                if (((gray[i][j] & 0x0000ff)) < grayMean) {
                    grayBackMean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayFrontMean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontValue = (int) (grayFrontMean / front);     // 前景中心
        int backValue = (int) (grayBackMean / back);        // 背景中心
        float G[] = new float[frontValue - backValue + 1];      // 方差数组
        int s = 0;
        Log.i(TAG, "Front: " + front + "**FrontValue: " + frontValue + "**BackValue: " + backValue);
        for (int i1 = backValue; i1 < frontValue + 1; i1++) {       // 以前景中心和背景中心为区间采用大津算法(OSTU)
            back = 0;
            front = 0;
            grayFrontMean = 0;
            grayBackMean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        grayBackMean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayFrontMean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayFrontMean = (int) (grayFrontMean / front);
            grayBackMean = (int) (grayBackMean / back);
            G[s] = (((float) back / area) * (grayBackMean - average) * (grayBackMean - average)
                    + ((float) front / area) * (grayFrontMean - average) * (grayFrontMean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontValue - backValue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }

        Log.i(TAG, "index: " + index + "FrontValue"+ frontValue + "BackValue:" + backValue);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backValue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        temp.setPixels(pix,0, width,0,0,width,height);
        return temp;
    }


}
