package bu.cs591.mosso.lambda;

public class RequestClass {

    String email;
    String latitude;
    String longitude;
    String type;
    String prevStep;
    String currentStep;
    String team;

    public RequestClass(String email, String latitude, String longitude, String type, String prevStep, String currentStep, String team) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.prevStep = prevStep;
        this.currentStep = currentStep;
        this.team = team;
    }

    public RequestClass() {

    }

    public String getEmail() {
        return email;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getTeam() {
        return team;
    }

    public String getPrevStep() {
        return prevStep;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    @Override
    public String toString() {
        return "RequestClass{" +
                "email='" + email + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", type='" + type + '\'' +
                ", prevStep='" + prevStep + '\'' +
                ", currentStep='" + currentStep + '\'' +
                ", team='" + team + '\'' +
                '}';
    }
}
