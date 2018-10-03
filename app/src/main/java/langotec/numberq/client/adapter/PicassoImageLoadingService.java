package langotec.numberq.client;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ss.com.bannerslider.ImageLoadingService;

/**
 * @author S.Shahini
 * @since 4/7/18
 */

public class PicassoImageLoadingService implements ImageLoadingService {
    public Context context;

    public PicassoImageLoadingService(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        Picasso
                .get()
                .load(url)
                .into(imageView);
    }

    @Override
    public void loadImage(int resource, ImageView imageView) {
        Picasso
                .get()
                .load(resource)
                .into(imageView);
    }

    @Override
    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        Picasso
                .get()
                .load(url).placeholder(placeHolder)
                .error(errorDrawable)
                .into(imageView);
    }
}
