import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.HashMap;


public class Input {


    public static void main(String[] args){
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

		try {
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetLine.open(format);
        targetLine.start();

        SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
        sourceLine.open(format);
        sourceLine.start();

        int numBytesRead;
        byte[] targetData = new byte[targetLine.getBufferSize() / 5];

        double[] doubles = new double[targetData.length / 3];
        double[] window = new double[7];

            double[] magnitudes = new double[doubles.length/2];
            double re = 0;
            double im = 0;
            while (true) {
                numBytesRead = targetLine.read(targetData, 0, targetData.length);

                for (int i = 0, j = 0; i != doubles.length; ++i, j += 3) {
                    doubles[i] = (double) ((targetData[j] & 0xff) |
                            ((targetData[j + 1] & 0xff) << 8) |
                            (targetData[j + 2] << 16));
                }

                DoubleFFT_1D fft = new DoubleFFT_1D(doubles.length);
                fft.realForward(doubles);

                for(int i = 0;i<doubles.length/2;i++){
                    re = doubles[2*i];
                    im = doubles[2*i+1];
                    magnitudes[i] = Math.sqrt(re*re+im*im);
                }
                double max = Double.MIN_VALUE;
                int index = 0;
                for(int i = 0;i<magnitudes.length;i++){
                    if(magnitudes[i]>max){
                        max = magnitudes[i];
                        index = i;
                    }

                }
                double freq = index * 44100 / doubles.length;
                shift(window, freq);

                System.out.println(mode(window));

            }


    }
		catch (Exception e) {
        System.err.println(e);
    }
}
    public static double mode(double []array)
    {
        HashMap<Double,Integer> hm = new HashMap<Double,Integer>();
        int max  = 1;
        double temp = 0;

        for(int i = 0; i < array.length; i++) {

            if (hm.get(array[i]) != null) {

                int count = hm.get(array[i]);
                count++;
                hm.put(array[i], count);

                if(count > max) {
                    max  = count;
                    temp = array[i];
                }
            }

            else
                hm.put(array[i],1);
        }
        return temp;
    }
public static void shift(double[] arr, double end){
        for(int i = 0;i<arr.length-1;i++){
            arr[i]  = arr[i+1];
        }
        arr[arr.length-1] = end;
}
}

