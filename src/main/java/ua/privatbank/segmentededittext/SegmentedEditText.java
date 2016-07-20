package ua.privatbank.segmentededittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import ua.privatbank.passwordsupportlibrary.R;

public class SegmentedEditText extends LinearLayout {

    private SegmentedEditTextDelegate delegate;
    private EditTextWatcher textWatcher;

    private int fieldNum;
    private int fieldCount;

    public SegmentedEditText(Context context) {
        this(context, null);
    }

    public SegmentedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SegmentedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SegmentedEditText, 0, 0);

        fieldNum = a.getInteger(R.styleable.SegmentedEditText_field_num, 2);
        fieldCount = a.getInteger(R.styleable.SegmentedEditText_field_count, 1);
        int fieldSpace = a.getDimensionPixelSize(R.styleable.SegmentedEditText_field_space, 0);

        textWatcher = new EditTextWatcher(fieldCount);

        for (int i = 0; i < fieldNum; i++) {
            EditText item = new EditText(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1F);
            params.setMargins(fieldSpace, 0, fieldSpace, 0);
            item.setLayoutParams(params);
            item.setGravity(Gravity.CENTER);
            item.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            item.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            item.setEnabled(false);
            item.setEms(10);
            if (i == 0) {
                item.setEnabled(true);
                item.requestFocus();
            }
            if (i == fieldNum - 1) {
                item.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            item.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (((EditText) v).getText().length() == 0 && indexOfChild(v) > 0) {
                            EditText prevItem = (EditText) getChildAt(indexOfChild(v) - 1);
                            String oldText = prevItem.getText().toString();
                            prevItem.setText(oldText.substring(0, oldText.length() - 1));
                            prevItem.setSelection(prevItem.length());
                            prevItem.setEnabled(true);
                            prevItem.requestFocus();
                            v.setEnabled(false);
                            return true;
                        }
                    }
                    return false;
                }
            });
            item.addTextChangedListener(textWatcher);
            addView(item);
        }
    }

    public void setDelegate(SegmentedEditTextDelegate delegate) {
        this.delegate = delegate;
    }

    public void setText(String text) {
        if (text == null || text.length() != fieldNum * fieldCount) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            EditText item = (EditText) getChildAt(i);
            int start = i * fieldCount;
            item.removeTextChangedListener(textWatcher);
            item.setText(text.substring(start, start + fieldCount));
            item.setEnabled(false);
        }
    }

    public void clear() {
        for (int i = 1; i < getChildCount(); i++) {
            ((EditText) getChildAt(i)).removeTextChangedListener(textWatcher);
            ((EditText) getChildAt(i)).setText("");
            ((EditText) getChildAt(i)).addTextChangedListener(textWatcher);
            getChildAt(i).setEnabled(false);
        }
        ((EditText) getChildAt(0)).removeTextChangedListener(textWatcher);
        ((EditText) getChildAt(0)).setText("");
        ((EditText) getChildAt(0)).addTextChangedListener(textWatcher);
        getChildAt(0).setEnabled(true);
        getChildAt(0).requestFocus();
    }

    private class EditTextWatcher implements TextWatcher {

        private final int fieldCount;

        private EditTextWatcher(int fieldCount) {
            this.fieldCount = fieldCount;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isLastItemFilled()) {
                EditText lastItem = getLastItem();
                lastItem.clearFocus();
                lastItem.setEnabled(false);
                if (delegate != null) {
                    StringBuilder text = new StringBuilder();
                    for (int i = 0; i < getChildCount(); i++) {
                        text.append(((EditText) getChildAt(i)).getText().toString());
                    }
                    delegate.onEnterText(text.toString());
                }
                return;
            }
            for (int i = 0; i < getChildCount(); i++) {
                if (((EditText) getChildAt(i)).getText().length() == fieldCount) {
                    getChildAt(i + 1).setEnabled(true);
                    getChildAt(i + 1).requestFocus();
                    getChildAt(i).setEnabled(false);
                }
            }
        }
    }

    private boolean isLastItemFilled() {
        return getLastItem().getText().length() == fieldCount;
    }

    private EditText getLastItem() {
        return (EditText) getChildAt(getChildCount() - 1);
    }

    public interface SegmentedEditTextDelegate {
        void onEnterText(String text);
    }

}