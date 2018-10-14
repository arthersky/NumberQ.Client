package langotec.numberq.client.menu;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;

//實作Serializable，讓intent或bundle可以丟此物件或存檔
public class Menu implements Serializable{

	private String imageURL;
	private String productName;
	private String productType;
	private String price;
	private String desc;
	private String waitTime;
	private String headName;
	private String branchName;
	private String from;
	private String HeadId;
	private String productId;
	private String headImageURL;
	private int waitNum, branchId;
	private int quantityNum = 1;
	private boolean available;
	private final short MAXQUANTITY = 200; //最大單筆菜單可加入購物車數量

	public Menu(String headName, String branchName, String headImageURL, String HeadId, String productId,
                String productType, String productName, String price, String image,
                boolean available, String desc, String waitTime, int branchId) {
		setHeadName(headName);
		setBranchName(branchName);
		setHeadImageURL(headImageURL);
		setHeadId(HeadId);
		setProductId(productId);
		setType(productType);
		setProductName(productName);
		setPrice(price);
		setImage(image);
		setAvailable(available);
		setDesc(desc);
		setFrom("fromMenuActivity");
		setWaitTime(waitTime);
		setBranchId(branchId);
	}

	//region getters & setters

	public String getImageURL() {
		return imageURL;
	}

	public void setImage(String image) {
		this.imageURL = image;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getType() {
		return productType;
	}

	public void setType(String type) {
		this.productType = type;
	}

	//Todo: 計算等待時間的功能
	public String getWaitTime() {
//		Calendar date = Calendar.getInstance();
//		String minutes = Integer.toString(date.get(Calendar.MINUTE));
//		String hours = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
//		if (minutes.length() == 1)
//			minutes = "0" + minutes;
//		if (hours.length() == 1)
//			hours = "0" + hours;
//		waitTime = hours + ":" + minutes;
		return waitTime;
	}

	public void setWaitTime(String waitTime) {
		this.waitTime = waitTime;
	}

	public int getWaitNum() {
		return waitNum;
	}

	public void setWaitNum(int waitNum) {
		if (waitNum <= 0)
			this.waitNum = waitNum;
		else
			this.waitNum = 0;
	}

	public void setQuantityNum(int quantityNum) {
		if (quantityNum >= 1 && quantityNum <= MAXQUANTITY)
			this.quantityNum = quantityNum;
		else if (quantityNum > MAXQUANTITY)
			this.quantityNum = MAXQUANTITY;
		else
			this.quantityNum = 1;
	}

	public int getQuantityNum() {
		return quantityNum;
	}

	public String getHeadName() {
		return headName;
	}

	public void setHeadName(String headName) {
		this.headName = headName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getHeadId() {
		return HeadId;
	}

	public void setHeadId(String headId) {
		HeadId = headId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

    public String getHeadImageURL() {
        return headImageURL;
    }

    public void setHeadImageURL(String headImageURL) {
        this.headImageURL = headImageURL;
    }
//endregion

	public void setImageView(ImageView imageView){
		Picasso
				.get()
				.load(imageURL)
				.resize(400, 300)
				.centerCrop()
				.into(imageView, new com.squareup.picasso.Callback() {
					@Override
					public void onSuccess() {
					}

					@Override
					public void onError(Exception e) {
					}
				});
	}
}