package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.GenericCardView;
import scal.io.liger.view.PreviewCardView;
import scal.io.liger.view.ReviewCardView;

public class ReviewCard extends GenericCard {

    public ReviewCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ReviewCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }
}