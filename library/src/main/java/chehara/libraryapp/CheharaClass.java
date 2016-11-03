package chehara.libraryapp;

import android.content.Context;
import android.content.Intent;

/**
 * Created by answerz on 3/11/16.
 */
public class CheharaClass {

    public static  void callIntent(Context context){
        Intent intent = new Intent(context, MainLIBActivity.class);
        context.startActivity(intent);
    }
}
