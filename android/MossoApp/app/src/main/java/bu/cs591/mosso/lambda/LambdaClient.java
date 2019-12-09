package bu.cs591.mosso.lambda;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;


public class LambdaClient {

    private static MyInterface instance;

    public static MyInterface getInstance() {
        return instance;
    }

    public static void generateInstance(Context context) {
        new LambdaClient(context);
    }

    private LambdaClient(Context context) {
        // Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-2:5a3bd3ab-04c2-4c3f-abfe-8bcecc8dad3c", // Identity pool ID
                Regions.US_EAST_2 // Region
        );

    // Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
        LambdaInvokerFactory factory = LambdaInvokerFactory.builder().context(context)
                .region(Regions.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();

    // Create the Lambda proxy object with a default Json data binder.
    // You can provide your own data binder by implementing
    // LambdaDataBinder.
        instance = factory.build(MyInterface.class);
    }

}
