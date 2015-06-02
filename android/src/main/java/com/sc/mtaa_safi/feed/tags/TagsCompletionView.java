package com.sc.mtaa_safi.feed.tags;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by ishuah on 6/2/15.
 */
public class TagsCompletionView extends TokenCompleteTextView<Tag> {

    public TagsCompletionView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    @Override
    protected View getViewForObject(Tag tag) {
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)layoutInflater.inflate(R.layout.tag_token, (ViewGroup)TagsCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.tagName)).setText(tag.getTagText());
        return view;
    }

    @Override
    protected Tag defaultObject(String completionText) {
        return new Tag(completionText);
    }

}
