package bu.cs591.mosso.lambda;

public class ResponseClass {

    String basicInfo;
    String additionalInfo;

    public ResponseClass(String basicInfo, String additionalInfo) {
        this.basicInfo = basicInfo;
        this.additionalInfo = additionalInfo;
    }

    public ResponseClass() {
    }

    public String getBasicInfo() {
        return basicInfo;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return "ResponseClass{" +
                "basicInfo='" + basicInfo + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
