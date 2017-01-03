package com.example.cameraalbum;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Freedom.Ly on 2017-1-3 13:49
 * Email  Freedom.JFL@Live.com
 */

public class Binary1 {

    private static final String TAG = "Binary1";

    public Bitmap Binary(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int gray[][] = new int[width][height];
        // 图像灰度化
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (inPixels[x] >> 16) & 0xff;
                int g = (inPixels[x] >> 8) & 0xff;
                int b = (inPixels[x]) & 0xff;
                int pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
            }
        }
        // 获取直方图
        int[] histoGram = new int[256];
        for (int i = 0; i < height; i++) {
            int tr = 0;
            for (int j = 0; j < width; j++) {
                int x = i * width + j;
                tr = (inPixels[x] >> 16) & 0xff;
                histoGram[tr]++;
            }
        }


        // 获取阈值
        int threshold = getMinThreshold(histoGram);
        Log.d(TAG, "Threshold: " + threshold);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < threshold) {
                    outPixels[in] = Color.rgb(0, 0, 0);
                } else {
                    outPixels[in] = Color.rgb(255, 255, 255);
                }
            }
        }
        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(outPixels, 0, width, 0, 0, width, height);
        return temp;
    }


    public static int getMinThreshold(int[] histGram) {
        int Y;
        int iter = 0;
        double[] histGramC = new double[256];
        double[] histGramCC = new double[256];

        for (Y = 0; Y < 256; Y++) {
            histGramC[Y] = histGram[Y];
            histGramCC[Y] = histGram[Y];
        }
        // 通过三点求均值来平滑直方图
        while (isDimodal(histGramCC) == false) {
            // 判断是否已经是双峰图像了
            histGramCC[0] = (histGramC[0] + histGramC[0] + histGramC[1]) / 3;   // 第一点
            for (Y = 1; Y < 255; Y++) {
                histGramCC[Y] = (histGramC[Y - 1] + histGramC[Y] + histGramC[Y + 1]) / 3;   // 中间的点
            }
            histGramCC[255] = (histGramC[254] + histGramC[255] + histGramC[255]) / 3; // 最后一点
            System.arraycopy(histGramCC, 0, histGramC, 0, histGramC.length);
            iter++;
            if (iter >= 1000) return -1;        // 直方图无法平滑为双峰的，返回错误代码
        }
        // 阈值即为两峰之间的最小值
        boolean peakFound = false;
        for (Y = 1; Y < 255; Y++) {
            if (histGramCC[Y - 1] < histGramCC[Y] && histGramCC[Y + 1] < histGramCC[Y])
                peakFound = true;
            if (peakFound == true && histGramCC[Y - 1] >= histGramCC[Y] && histGramCC[Y + 1] >= histGramCC[Y])
                return Y - 1;
        }
        return -1;
    }

    private static boolean isDimodal(double[] histGram) {     // 检测直方图是否为双峰的
        int count = 0;
        for (int i = 1; i < 255; i++) {
            if (histGram[i - 1] < histGram[i] && histGram[i + 1] < histGram[i]) {
                count++;
                if (count > 2) return false;
            }
        }
        if (count == 2)
            return true;
        else
            return false;
    }

}
