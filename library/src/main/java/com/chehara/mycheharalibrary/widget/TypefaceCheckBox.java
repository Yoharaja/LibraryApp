package com.chehara.mycheharalibrary.widget;





import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;

import com.chehara.mycheharalibrary.R;

public class TypefaceCheckBox extends CheckBox {
    private static LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(12);
    String TAG="TypefaceEditText";
    public TypefaceCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Get our custom attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TypefaceCheckBox, 0, 0);
        String typefaceName = a.getString(R.styleable.TypefaceCheckBox_checkBoxTypeface);
        if(typefaceName==null ){
        	typefaceName="Roboto-Thin";
        }
        
        try {
            
            if (!isInEditMode() && !TextUtils.isEmpty(typefaceName)) {
                Typeface typeface = sTypefaceCache.get(typefaceName);
                
                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(),
                            String.format("fonts/%s.ttf", typefaceName));
                    sTypefaceCache.put(typefaceName, typeface);
                }
                setTypeface(typeface);
                
                
                setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        }catch (Exception e) {
            Log.e(TAG, String.format("Error occured when trying to apply %s font",typefaceName));
            e.printStackTrace();
        } finally {
            a.recycle();
        }
    }

}
