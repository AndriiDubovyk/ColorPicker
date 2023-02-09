package com.andriidubovyk.colorpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ColorPicker extends FrameLayout {

    private Bitmap colorImageBitmap;
    private  GradientDrawable gd;
    private float hsv[] = new float[3];

    private ImageView colorPaletteImage;
    private ImageView colorPaletteThumb;
    private EditText colorText;
    private View colorDisplay;
    private View startColorDisplay;
    private SeekBar colorValueSeekBar;
    private SeekBar colorAlphaSeekBar;
    private FrameLayout alphaBlock;
    private RecyclerView predefinedColorView;

    private Context context;

    private TypedArray attributes;


    private ArrayList<Integer> colorList = new ArrayList<>();
    private boolean showAlpha;
    private boolean showHexText;
    private int startColor;
    private static boolean SHOW_ALPHA_DEFAULT_VALUE = true;
    private static boolean SHOW_HEX_TEXT_DEFAULT_VALUE = true;
    private static int START_COLOR_DEFAULT_VALUE = 0xffffffff;



    // HSV with alpha
    private ColorHSVA colorHSVA;

    public ColorPicker(Context context) {
        super(context);
        init(context);
    }

    public ColorPicker(Context context, AttributeSet attrs){
        super(context, attrs);
        attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ColorPicker,
                0, 0);
        init(context);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ColorPicker,
                0, 0);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        showAlpha = attributes.getBoolean(R.styleable.ColorPicker_showAlpha, SHOW_ALPHA_DEFAULT_VALUE);
        showHexText = attributes.getBoolean(R.styleable.ColorPicker_showHexText, SHOW_HEX_TEXT_DEFAULT_VALUE);
        startColor = attributes.getColor(R.styleable.ColorPicker_startColor, START_COLOR_DEFAULT_VALUE);
        int colorsId = attributes.getResourceId(R.styleable.ColorPicker_predefinedColors, 0);
        if(colorsId!=0) {
            int[] colorsArray = attributes.getResources().getIntArray(colorsId);
            if(colorsArray!=null) {
                for (int c : colorsArray) {
                    colorList.add(c);
                }
            }

        }
        colorHSVA = new ColorHSVA(0, 1, 1);
        View mainView = LayoutInflater.from(context).inflate(R.layout.color_picker_layout, this);

        colorPaletteImage = mainView.findViewById(R.id.color_palette);

        colorPaletteThumb = mainView.findViewById(R.id.color_palette_thumb);

        colorText = mainView.findViewById(R.id.color_text);
        colorText.addTextChangedListener(colorTextWatcher);

        startColorDisplay = mainView.findViewById(R.id.color_display);

        predefinedColorView = mainView.findViewById(R.id.predefined_colors_view);
        final PredefinedColorsAdapter adapter = new PredefinedColorsAdapter(colorList);
        predefinedColorView.setAdapter(adapter);
        predefinedColorView.setLayoutManager(new LinearLayoutManager(getContext()));
        predefinedColorView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, predefinedColorView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        colorHSVA = new ColorHSVA(adapter.getColor(position));
                        update(true, true, true, true);
                    }
                    @Override public void onLongItemClick(View view, int position) {}
                })
        );

        colorDisplay = mainView.findViewById(R.id.color_display);

        colorAlphaSeekBar = mainView.findViewById(R.id.color_alpha_line);
        colorAlphaSeekBar.setOnSeekBarChangeListener(colorAlphaSeekBarListener);

        colorValueSeekBar = mainView.findViewById(R.id.color_value_line);
        colorValueSeekBar.setOnSeekBarChangeListener(colorValueSeekBarListener);

        colorPaletteImage.setDrawingCacheEnabled(true);
        colorPaletteImage.buildDrawingCache();
        colorPaletteImage.setOnTouchListener(colorPaletteOnTouchListener);

        alphaBlock = mainView.findViewById(R.id.alpha_block);
        updateAlphaBlockVisibility();
        updateHexTextVisibility();
        setStartColor(startColor);
    }

    public int getSelectedColor() {
        return colorHSVA.getHexValue();
    }

    public void setStartColor(int color) {
        colorHSVA = new ColorHSVA(startColor);
        startColorDisplay.setBackgroundColor(startColor);
        update(true, true, true, true);
    }

    private void updateHexTextVisibility() {
        if(showHexText) {
            colorText.setVisibility(VISIBLE);
        } else {
            colorText.setVisibility(GONE);
        }
    }

    private void updateAlphaBlockVisibility() {
        if(showAlpha) {
            alphaBlock.setVisibility(VISIBLE);
        } else {
            alphaBlock.setVisibility(GONE);
        }
    }

    public void setShowAlpha(boolean showAlpha) {
        this.showAlpha = showAlpha;
        updateAlphaBlockVisibility();
    }

    private void removeColorTextFocus() {
        colorText.clearFocus();
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }
    private void updateColorDisplay() {
        colorDisplay.setBackgroundColor(colorHSVA.getHexValue());
    }
    private void updateColorText() {
        removeColorTextFocus();
        colorText.setText(colorHSVA.getHexString(showAlpha));
    }

    private void setColorPaletteThumbPos(float x, float y) {
        colorPaletteThumb.setX(x - colorPaletteThumb.getWidth()/2);
        colorPaletteThumb.setY(y - colorPaletteThumb.getHeight()/2);
    }

    private void updateAlphaSeekBar() {
        int color = Color.HSVToColor(new float[]{ colorHSVA.getHue(), colorHSVA.getSaturation(), 1f });
        gd = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                new int[] {color,0x00ffffff});
        gd.setCornerRadius(0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            colorAlphaSeekBar.setBackground(gd);
        }

        colorAlphaSeekBar.setProgress(Math.round(colorHSVA.getAlpha()*colorAlphaSeekBar.getMax()));
    }
    private void updateValueSeekBar() {
        int color = Color.HSVToColor(new float[]{ colorHSVA.getHue(), colorHSVA.getSaturation(), 1f });
        gd = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                new int[] {color,0xFF000000});
        gd.setCornerRadius(0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            colorValueSeekBar.setBackground(gd);
        }


        colorValueSeekBar.setProgress(Math.round(colorHSVA.getValue()*colorValueSeekBar.getMax()));
    }
    private static float convertDegToRad(float degree)
    {
        float pi = 3.14159f;
        return (degree * (pi / 180.0f));
    }
    private void updateColorPalette() {
        int centerX = colorPaletteImage.getWidth()/2;
        int centerY = colorPaletteImage.getHeight()/2;
        int length = colorPaletteImage.getWidth()/2;
        double angleRad = convertDegToRad(colorHSVA.getHue());
        float dist = colorHSVA.getSaturation() * length;
        float x = (float)(centerX+dist*Math.sin(angleRad));
        float y = (float)(centerY-dist*Math.cos(angleRad));
        setColorPaletteThumbPos(x, y);
    }
    private void update(
            boolean updateColorText, boolean updateColorPalette,
            boolean updateValueSeekBar, boolean updateAlphaSeekBar) {
        if(updateColorText) {
            updateColorText();
        }
        if(updateColorPalette) {
            updateColorPalette();
        }
        if(updateValueSeekBar) {
            updateValueSeekBar();
        }
        if(updateAlphaSeekBar) {
            updateAlphaSeekBar();
        }
        updateColorDisplay();
    }


    private TextWatcher colorTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(colorText.hasFocus()) {
                ColorHSVA tmpColorHSVA = ColorHSVA.fromHexString(charSequence.toString(), showAlpha);
                if(tmpColorHSVA!=null) {
                    colorHSVA = tmpColorHSVA;
                    update(false, true, true, true);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private SeekBar.OnSeekBarChangeListener colorAlphaSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
            if(fromUser) {
                colorHSVA.setAlpha(((float)colorAlphaSeekBar.getProgress())/colorAlphaSeekBar.getMax());
                update(true, false, false, false);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekbar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekbar) {}
    };

    private SeekBar.OnSeekBarChangeListener colorValueSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
            if(fromUser) {
                colorHSVA.setValue(((float)colorValueSeekBar.getProgress())/colorValueSeekBar.getMax());
                update(true, false, false, false);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekbar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekbar) {}
    };


    private OnTouchListener colorPaletteOnTouchListener = new OnTouchListener() {
        int colorPaletteValue = -1;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                colorImageBitmap = colorPaletteImage.getDrawingCache();
                int width = colorImageBitmap.getWidth();
                int height  = colorImageBitmap.getHeight();
                int pixel;
                // find closest position
                int x = (int)event.getX();
                int y =(int)event.getY();
                while (x < 0 || x >= width || y < 0 || y >= height || colorImageBitmap.getPixel(x, y) == 0) {
                    if(x<0) x=0;
                    if(x>=width) x=width-1;
                    if(y<0) y=0;
                    if(y>=height) y=height-1;
                    if(Math.abs(x - width/2)>Math.abs(y - height/2)) {
                        if(x > width/2) x--;
                        else x++;
                    } else {
                        if(y > height/2) y--;
                        else y++;
                    }
                }

                try {
                    pixel = colorImageBitmap.getPixel(x, y);
                } catch(IllegalArgumentException e) {
                    return false;
                }

                setColorPaletteThumbPos(x, y);

                colorPaletteValue = pixel;

                Color.colorToHSV(pixel, hsv);
                colorHSVA.setHue(hsv[0]);
                colorHSVA.setSaturation(hsv[1]);

                update(true, false, true, true);

            }
            return true;
        }
    };


}
