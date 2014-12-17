package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.MediumCardView;
import scal.io.liger.view.DisplayableCard;


public class MediumCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String header;

    public MediumCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new MediumCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof MediumCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF MediumCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        MediumCard castCard = (MediumCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
    }
}
