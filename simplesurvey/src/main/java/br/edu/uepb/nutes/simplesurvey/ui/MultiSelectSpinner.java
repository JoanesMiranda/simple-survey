/*
 * Copyright (c) 2018 NUTES/UEPB
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.edu.uepb.nutes.simplesurvey.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import br.edu.uepb.nutes.simplesurvey.R;

public class MultiSelectSpinner extends LinearLayout implements DialogInterface.OnMultiChoiceClickListener {
    private final String TAG = "MultiSelectSpinner";

    private final Context context;
    private OnSpinnerListener mListener;
    private AppCompatSpinner mSpinner;
    private AlertDialog.Builder dialogBuilder;

    private List<String> items;
    private List<Boolean> itemsSelected;
    private CustomMultiSpinnerAdapter mAdapter;
    private LinearLayout boxButton;
    private AppCompatImageButton mButton;

    protected String hint;
    protected String messageEmpty;
    protected String titleDialogAddNewItem;
    @ColorInt
    protected int colorSelectedText;
    @ColorInt
    protected int colorBackgroundTint;
    protected boolean enabledAddNewItem;
    protected int textAlign;

    public MultiSelectSpinner(Context context) {
        super(context);
        this.context = context;

        initControl();
    }

    public MultiSelectSpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initControl();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiSelectSpinner);
        if (typedArray != null && typedArray.length() > 0) {
            try {
                setItems(typedArray.getTextArray(R.styleable.MultiSelectSpinner_android_entries));
                setHint(typedArray.getString(R.styleable.MultiSelectSpinner_android_hint));
                setMessageEmpty(typedArray.getString(R.styleable.MultiSelectSpinner_messageEmpty));
                setTitleDialogAddNewItem(typedArray.getString(R.styleable.MultiSelectSpinner_titleDialogAddNewItem));
                setColorSelectedText(typedArray.getColor(R.styleable.MultiSelectSpinner_colorSelectedText, Color.GRAY));
                setColorBackgroundTint(typedArray.getColor(R.styleable.MultiSelectSpinner_colorBackgroundTint, Color.GRAY));
                setEnabledAddNewItem(typedArray.getBoolean(R.styleable.MultiSelectSpinner_colorBackgroundTint, true));
                setTextAlign(typedArray.getInt(R.styleable.MultiSelectSpinner_textAlign, 0));
            } finally {
                typedArray.recycle();
            }
        }
    }

    /**
     * Load xml layout and elements.
     */
    private void initControl() {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ui_custom_spinner_layout, this);

        this.enabledAddNewItem = true;
        this.items = new ArrayList<>();
        this.itemsSelected = new ArrayList<>();
        this.dialogBuilder = new AlertDialog.Builder(context);
        this.hint = context.getResources().getString(R.string.survey_select_the_answers);
        this.messageEmpty = context.getResources().getString(R.string.survey_please_add_new_item);
        this.titleDialogAddNewItem = context.getResources().getString(R.string.survey_add_new_item);

        assignUiElements();
        assignListeners();
        initAdapter();
    }

    /**
     * get instance elements.
     */
    private void assignUiElements() {
        this.mSpinner = findViewById(R.id.custom_select_spinner);
        this.boxButton = findViewById(R.id.custom_box_add_item);
        this.mButton = findViewById(R.id.custom_add_item_imageButton);
    }

    /**
     * init handlers
     */
    private void assignListeners() {
        if (this.enabledAddNewItem) this.mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogAddNewItem();
            }
        });

        this.mSpinner.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) openDialogItems();

                // causes the default drop down not to open, because we want the AlertDialog.
                return true;
            }
        });
    }

    /**
     * Init adapter spinner.
     */
    private void initAdapter() {
        this.mAdapter = new CustomMultiSpinnerAdapter(getContext(),
                android.R.layout.simple_spinner_item, getItems());
        this.mSpinner.setAdapter(this.mAdapter);

        refreshComponent();
    }

    /**
     * Open AlertDialog with the inputItems to choose.
     *
     * @return true
     */
    public boolean openDialogItems() {
        if (this.items.isEmpty())
            this.dialogBuilder.setMessage(messageEmpty);
        this.dialogBuilder.setTitle(hint);

        String[] _arrayItems = itemsToArray();
        boolean[] _arraySelectedItems = itemsSelectedToArray();

        this.dialogBuilder.setMultiChoiceItems(_arrayItems, _arraySelectedItems, this);
        this.dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mListener != null)
                        mListener.onMultiItemSelected(getSelectedItems(), getIndexSelectedItems());
                }
            });
        }
        this.dialogBuilder.show();

        return true;
    }

    /**
     * Items selected List to Array.
     *
     * @return boolean[]
     */
    private boolean[] itemsSelectedToArray() {
        boolean[] result = new boolean[this.itemsSelected.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = this.itemsSelected.get(i);

        return result;
    }

    /**
     * Convert {@link List < String >} in [].
     *
     * @return String
     */
    private String[] itemsToArray() {
        if (this.hint != null)
            this.items.remove(getHint());

        String[] result = new String[this.items.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = this.items.get(i);
        return result;
    }

    /**
     * Refresh components.
     */
    private void refreshComponent() {
        if (this.mSpinner == null) return;

        ViewCompat.setBackgroundTintList(this.mSpinner, ColorStateList.valueOf(getColorBackgroundTint()));
        mButton.setColorFilter(getColorBackgroundTint());

        if (!enabledAddNewItem) this.boxButton.setVisibility(View.GONE);
        else this.boxButton.setVisibility(View.VISIBLE);

        refreshAdapter();
    }

    /**
     * Refresh adapter spinner.
     */
    private void refreshAdapter() {
        this.mAdapter.clear();
        this.mAdapter.add(getHint());
    }

    /**
     * Init list inputItems default selecteds;
     */
    private void initListSelectedItems() {
        this.itemsSelected = new ArrayList<>(this.items.size() + 1); // + 1 hint

        for (int i = 0; i < this.items.size(); i++)
            this.itemsSelected.add(false);
    }

    /**
     * Clear adapter spinner.
     */
    public void clear() {
        this.mAdapter.clear();
        refreshAdapter();
    }

    public List<String> getItems() {
        return this.items;
    }

    public void setItems(List<String> items) {
        this.items = items;
        initListSelectedItems();
    }

    public void setItems(CharSequence[] entries) {
        this.items = new ArrayList<>();
        if (entries == null) return;

        for (CharSequence c : entries)
            this.items.add(String.valueOf(c));

        initListSelectedItems();
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        if (hint != null) this.hint = hint;
        refreshAdapter();
    }

    public int getColorSelectedText() {
        return this.colorSelectedText;
    }

    public void setColorSelectedText(int colorSelectedText) {
        this.colorSelectedText = colorSelectedText;
        refreshComponent();
    }

    public int getColorBackgroundTint() {
        return this.colorBackgroundTint;
    }

    public void setColorBackgroundTint(int colorBackgroundTint) {
        this.colorBackgroundTint = colorBackgroundTint;
        refreshComponent();
    }

    public String getMessageEmpty() {
        return this.messageEmpty;
    }

    public void setMessageEmpty(String messageEmpty) {
        if (messageEmpty != null) this.messageEmpty = messageEmpty;
    }

    public String getTitleDialogAddNewItem() {
        return this.titleDialogAddNewItem;
    }

    public void setTitleDialogAddNewItem(String titleDialogAddNewItem) {
        if (titleDialogAddNewItem != null) this.titleDialogAddNewItem = titleDialogAddNewItem;
    }

    /**
     * Selects inputItems according to values from list passed as parameter
     *
     * @param selection {@link List}
     */
    private void internalSelection(List<String> selection) {
        for (int i = 0; i < this.itemsSelected.size(); i++)
            this.itemsSelected.set(i, false);

        for (String sel : selection) {
            int index = this.items.indexOf(sel);
            if (index > -1 && index < this.items.size())
                this.itemsSelected.set(index, true);
//            for (int j = 0; j < this.inputItems.size(); ++j) {
//                if (this.inputItems.get(j).equals(sel))
//                    this.itemsSelected.set(j, true);
//            }
        }
    }

    /**
     * Build string inputItems selected.
     *
     * @return {@link String}
     */
    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < this.items.size(); ++i) {
            if (this.itemsSelected.get(i)) {
                if (foundOne) sb.append(", ");

                foundOne = true;
                sb.append(this.items.get(i));
            }
        }
        return sb.toString();
    }

    public void setEnabledAddNewItem(boolean enabled) {
        this.enabledAddNewItem = enabled;
        refreshComponent();
    }

    public void setTextAlign(int textAlign) {
        this.textAlign = textAlign;
        initAdapter();
    }

    /**
     * Set {@link OnSpinnerListener}
     *
     * @param listener
     */
    public void setOnSpinnerListener(OnSpinnerListener listener) {
        this.mListener = listener;
    }

    /**
     * Select item in list by index.
     *
     * @param index
     */
    public void selection(int index) {
        if (index >= 0 && index < this.itemsSelected.size()) this.itemsSelected.set(index, true);
        else throw new IllegalArgumentException("Index index is out of bounds.");

        this.mAdapter.clear();
        this.mAdapter.add(buildSelectedItemString());
    }

    /**
     * Select item in list by item name.
     *
     * @param item
     */
    public void selection(String item) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).equals(item)) {
                this.itemsSelected.set(i, true);
                break;
            }
        }

        this.mAdapter.clear();
        this.mAdapter.add(buildSelectedItemString());
    }

    /**
     * Selects inputItems according to values from list passed as parameter
     *
     * @param selections {@link List}
     */
    public void selection(List selections) {
        if (selections != null && selections.size() > 0) {
            if (selections.get(0) instanceof Integer) {
                int[] _selections = new int[selections.size()];
                for (int i = 0; i < selections.size(); i++)
                    _selections[i] = (int) selections.get(i);
                internalSelection(_selections);
            } else if (selections.get(0) instanceof String) {
                internalSelection(selections);
            }
        }
    }

    /**
     * Select array inputItems in list.
     *
     * @param selectedIndicies
     */
    private void internalSelection(int[] selectedIndicies) {
        for (int i = 0; i < this.itemsSelected.size(); i++)
            this.itemsSelected.set(i, false);

        for (int index : selectedIndicies) {
            if (index >= 0 && index < this.itemsSelected.size())
                this.itemsSelected.set(index, true);
        }

        this.mAdapter.clear();
        this.mAdapter.add(buildSelectedItemString());
    }

    private int[] internalGetIndexSelectedItems() {
        List<Integer> _temp = getIndexSelectedItems();
        int[] result = new int[_temp.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = _temp.get(i);

        return result;
    }

    /**
     * Get {@link List < String >} inputItems selected.
     *
     * @return List {@link List < String >}
     */
    public List<String> getSelectedItems() {
        this.items.remove(getHint());

        List<String> selection = new LinkedList<>();
        for (int i = 0; i < this.items.size(); ++i)
            if (this.itemsSelected.get(i)) selection.add(this.items.get(i));

        return selection;
    }


    /**
     * Get indexes of selected inputItems.
     *
     * @return {@link List < Integer >}
     */
    public List<Integer> getIndexSelectedItems() {
        this.items.remove(getHint());

        List<Integer> selection = new LinkedList<>();
        for (int i = 0; i < this.items.size(); ++i)
            if (this.itemsSelected.get(i)) selection.add(i);

        return selection;
    }

    /**
     * Check if there is at least one item selected.
     *
     * @return Boolean
     */
    public boolean thereAreSelectedItems() {
        for (Boolean item : this.itemsSelected)
            if (item) return true;
        return false;
    }

    /**
     * Add new item Dynamically.
     *
     * @param item {@link String}
     */
    public void addItem(String item) {
        if (item == null) throw new IllegalArgumentException("Item is null");
        this.items.add(item);
        this.itemsSelected.add(true);

        String result = buildSelectedItemString();
        if (!result.isEmpty()) {
            this.mAdapter.clear();
            this.mAdapter.add(result);
        }
        this.mAdapter.notifyDataSetChanged();
    }


    /**
     * Open dialog to add new item.
     */
    public void openDialogAddNewItem() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        if (this.titleDialogAddNewItem != null) alertBuilder.setTitle(this.titleDialogAddNewItem);

        // Creating EditText
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSingleLine();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        params.topMargin = 20;
        input.setLayoutParams(params);
        FrameLayout layout = new FrameLayout(context);
        layout.addView(input);
        alertBuilder.setView(layout);

        // Action add
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String valueItem = String.valueOf(input.getText()).trim();
                if (valueItem.isEmpty() || items.contains(valueItem)) {
                    if (mListener != null) mListener.onAddNewItemCancel();
                } else {
                    addItem(valueItem);
                    if (mListener != null) {
                        mListener.onMultiItemSelected(getSelectedItems(), getIndexSelectedItems());
                        mListener.onAddNewItemSuccess(valueItem, items.size() - 1);
                    }
                }
            }
        });

        // Action cancel
        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) mListener.onAddNewItemCancel();
            }
        });


        // Cancellation action for any other reason.
        alertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mListener != null) mListener.onAddNewItemCancel();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            alertBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    closeKeyBoard();
                }
            });
        }

        alertBuilder.show();
        openKeyBoard(input);
    }

    /**
     * Open Keyboard.
     *
     * @param view
     */
    private void openKeyBoard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Close Keyboad.
     */
    private void closeKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) (this.context)
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (which < this.itemsSelected.size()) {
            this.itemsSelected.set(which, isChecked);

            String result = buildSelectedItemString();
            if (result.isEmpty()) {
                this.mAdapter.clear();
                this.mAdapter.add(getHint());
            } else {
                this.mAdapter.clear();
                this.mAdapter.add(result);
            }
        } else {
            throw new IllegalArgumentException("Argument 'which' is out of bounds.");
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putIntArray("indexSelectedItems", internalGetIndexSelectedItems());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            internalSelection(bundle.getIntArray("indexSelectedItems"));
        }
        super.onRestoreInstanceState(state);
    }

    private class CustomMultiSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {
        private final Context context;
        private List<String> _items;

        public CustomMultiSpinnerAdapter(@NonNull Context context, int textViewResourceId, List<String> _items) {
            super(context, textViewResourceId, _items);
            this.context = context;
            this._items = _items;
        }

        public int getCount() {
            return _items.size();
        }

        public String getItem(int i) {
            return _items.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView txt = new TextView(context);
            txt.setText(_items.get(position));
            txt.setTextSize(16);
            txt.setTextColor(getColorSelectedText());

            // text align
            if (textAlign == 1) txt.setGravity(Gravity.CENTER);
            else if (textAlign == 2) txt.setGravity(Gravity.RIGHT);
            else txt.setGravity(Gravity.LEFT);

            return txt;
        }
    }

    /**
     * Listener spinner.
     */
    public interface OnSpinnerListener {
        void onMultiItemSelected(@NonNull List<String> items, @NonNull List<Integer> indexItems);

        void onAddNewItemSuccess(@NonNull String item, @NonNull int indexItem);

        void onAddNewItemCancel();
    }
}
