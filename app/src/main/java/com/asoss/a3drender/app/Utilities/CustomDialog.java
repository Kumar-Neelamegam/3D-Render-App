package com.asoss.a3drender.app.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.asoss.a3drender.app.R;

public class CustomDialog extends Dialog implements
        View.OnClickListener {

    private final Context c;
    private final Dialog d;
    private possitiveOnClick possitiveOnClick = null;
    private negativeOnClick negativeOnClick = null;
    private LinearLayout layout = null;
    private Button yes = null;
    private Button no = null;
    private TextView title = null;
    private TextView description = null;
    private AppCompatImageView icon = null;

    public CustomDialog(Context a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.d = this;
        show();
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_common_dialog);
        yes = findViewById(R.id.bt_close_yes);
        no = findViewById(R.id.bt_close_no);
        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        description = findViewById(R.id.content);
        layout = findViewById(R.id.panelcolor);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        d.setCancelable(false);

    }


    public final CustomDialog setLayoutColor(int color) {
        this.layout.setBackgroundColor(c.getResources().getColor(color));
        return this;
    }

    public final CustomDialog setImage(int image) {
        this.icon.setImageResource(image);


        //     Drawable d = this.icon.getDrawable();
//       ((AnimatedVectorDrawable)d).start();

        return this;
    }

    public final CustomDialog setTitle(String title) {
        this.title.setText(title);
        return this;
    }

    public final CustomDialog setTitleColor(int title) {
        this.title.setTextColor(title);
        return this;
    }

    public final CustomDialog setTitleBackgroundColor(int color) {
        this.title.setBackgroundColor(color);
        return this;
    }

    public final CustomDialog setDescription(String title) {
        this.description.setText(title);
        return this;
    }

    public final CustomDialog setDescriptionColor(int color) {
        this.description.setTextColor(color);
        return this;
    }

    public final CustomDialog setDescriptionBackgroundColor(int color) {
        this.description.setBackgroundColor(color);
        return this;
    }

    public final CustomDialog setPossitiveButtonTitle(String color) {
        this.yes.setText(color);
        return this;
    }

    public final CustomDialog setNegativeButtonTitle(String color) {
        this.no.setText(color);
        return this;
    }


    public final CustomDialog setPassitiveButtonBackground(int color) {
        this.yes.setBackgroundColor(color);
        return this;
    }

    public final CustomDialog setNegativeButtonBackground(int color) {
        this.no.setBackgroundColor(color);
        return this;
    }


    public final CustomDialog setNegativeButtonVisible(int visible) {
        this.no.setVisibility(visible);
        return this;
    }


    public final CustomDialog setOnPossitiveListener(possitiveOnClick possitiveListener) {
        this.possitiveOnClick = possitiveListener;
        return this;
    }

    public final CustomDialog setOnNegativeListener(negativeOnClick negativeListener) {
        this.negativeOnClick = negativeListener;
        return this;
    }


    @Override
    public final void onClick(View view) {

        switch (view.getId()) {
            case R.id.bt_close_yes:
                //c.finish();
                try {
                    possitiveOnClick.onPossitivePerformed();
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
            case R.id.bt_close_no:
                dismiss();
                //negativeOnClick.onNegativePerformed(d);
                break;
            default:
                break;
        }

        dismiss();
    }

    public interface possitiveOnClick {
        void onPossitivePerformed();
    }

    interface negativeOnClick {
        void onNegativePerformed();
    }

}