package bu.cs591.mosso;

public class FitData {
    static int step = 0;
    static int calory = 0;
    static int fitStep = 0;
    static int sensorStep = 0;
    static int fitCalory = 0;
    static int sensorCalory = 0;

    public static int getStep() {

        step = fitStep + sensorStep;
        return step;
    }

    public static void setStep(int s) {
        step = s;
    }

    public static void setFitStep(int s) {
        fitStep = s;
    }

    public static int getFitStep() {
        return fitStep;
    }

    public static int getSensorStep() {
        return sensorStep;
    }

    public static void setSensorStep(int s) {
        sensorStep = s;
    }

    public static int getCalory() {
        calory = fitCalory + sensorCalory;
        return calory;
    }

    public static void setCalory(int c) {
        calory = c;
    }

    public static void setFitCalory(int c) {
        fitCalory = c;
    }

    public static int getFitCalory() {
        return fitCalory;
    }

    public static void setSensorCalory(int c) {
        sensorCalory = c;
    }

    public static int getSensorCalory() {
        return sensorCalory;
    }

}
