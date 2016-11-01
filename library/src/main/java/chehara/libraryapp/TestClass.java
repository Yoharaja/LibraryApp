package chehara.libraryapp;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by answerz on 1/11/16.
 */
public class TestClass {

    public static void testToast(Context context, String Msg) {
        Toast.makeText(context, Msg, Toast.LENGTH_SHORT).show();
    }

    public static void testActivity(Context context) {

        Intent intent = new Intent(context, MainLIBActivity.class);
        context.startActivity(intent);

    }
}
