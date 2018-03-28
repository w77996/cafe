package com.w77996.cafeprint;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.w77996.cafeprint.activity.ImageCropActivity;
import com.w77996.cafeprint.camera.CameraActivity;
import com.w77996.cafeprint.inter.GlideImageLoader;
import com.w77996.cafeprint.utils.Constants;
import com.w77996.cafeprint.utils.Utils;
import com.w77996.cafeprint.utils.permission.PermissionsActivity;
import com.w77996.cafeprint.utils.permission.PermissionsChecker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    /**
     * 拍照按钮
     */
    Button mPhoto;
    /**
     * 选择图片按钮
     */
    Button mPic;
    /**
     * 上传按钮
     */
    Button mInput;
    /**
     * 宽度
     */
    int mWidth;
    /**
     * Imagepicker初始化
     */
    private ImagePicker imagePicker;
    /**
     * 中部mImage
     */
    private ImageView mImage;
    String mUploadUrl ="";
    /**
     * 权限检测器
     */
    private PermissionsChecker mPermissionsChecker;
    private String mImagePath;
    /**
     * 所需的全部权限
     */
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化zxing二维码扫描
        ZXingLibrary.initDisplayOpinion(this);
        //初始化权限
        mPermissionsChecker = new PermissionsChecker(this);
        initView();
        initClick();
        initImageView();
    }

    /**
     * 初始化click事件
     */
    private void initClick() {
        mPhoto.setOnClickListener(this);
        mPic.setOnClickListener(this);
        mInput.setOnClickListener(this);
    }

    /**
     * 初始化界面控件
     */
    private void initView() {

        mImage = (ImageView)findViewById(R.id.img_center);
        mInput = (Button)findViewById(R.id.btn_input);
        mPhoto = (Button)findViewById(R.id.btn_photo);
        mPic = (Button)findViewById(R.id.btn_select_pic);
        mWidth =  Utils.getDisplayWidth(this);

    }

    private void initImageView(){
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setMultiMode(false);
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.CIRCLE);  //裁剪框的形状
        imagePicker.setFocusWidth(mWidth);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(mWidth);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
       /* imagePicker.setOutPutX(mWidth);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(mWidth);//保存文件的高度。单位像素*/
    }
    /**
     * 初始化菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 重写OptionsItemSelected(MenuItem item)来响应菜单项(MenuItem)的点击事件（根据id来区分是哪个item）
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplication(), CaptureActivity.class);
                startActivityForResult(intent, Constants.ZXING_CODE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onResume() {
        super.onResume();

        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    /**
     * 开启权限Activity
     */
    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, Constants.PERMISSION_CODE, PERMISSIONS);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //拍照
            case R.id.btn_photo:
                Intent CameraIntent = new Intent(this, CameraActivity.class);
                //intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
                startActivityForResult(CameraIntent, Constants.PICTURE_CODE);
                //startActivity(intent);
                break;
            //选择图片
            case R.id.btn_select_pic:
                Intent imageSelectIntent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(imageSelectIntent, Constants.PIC_SELECT_CODE);
                break;
            //上传
            case R.id.btn_input:
                upLoadImage();
                break;
        }
    }

    private void upLoadImage() {
        if (mUploadUrl == null || "".equals(mUploadUrl)){
            Toast.makeText(this,"请扫描上传",Toast.LENGTH_SHORT).show();
            return;
        }
        OkGo.<String>post(mUploadUrl)
                .tag(this)
                .isMultipart(true)
                .params("image",mImagePath)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.d("MainActivity","uplaod success "+response.toString());
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.d("MainActivity","uplaod error "+response.toString());
                    }
                });
            }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//图片选择返回
            if (data != null && requestCode == Constants.PIC_SELECT_CODE) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                Log.d("MainActivity",images.size()+" " +images.get(0).path+"name :" +images.get(0).name);

                imagePicker.getImageLoader().displayImage(MainActivity.this, images.get(0).path, mImage, mWidth, mWidth);

            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == Constants.ZXING_CODE) {//二维码返回
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();

                    if(result!=null && "".equals(result)){
                        mUploadUrl = result;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }else if(requestCode == Constants.PICTURE_CODE){//拍照返回
            if (null != data) {
                String imagePath = data.getStringExtra("imagefile");
                Log.d("MainActivity",imagePath);
               /* Intent intent = new Intent();
                intent.putExtra(Constants.IMAGE_CROP_CODE, imagePath);
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);*/
               Intent intent = new Intent(MainActivity.this, ImageCropActivity.class);
                intent.putExtra("imagefile",imagePath);
                startActivityForResult(intent,Constants.IMAGE_CROP_CODE);
            }
        }else if(requestCode == Constants.IMAGE_CROP_CODE){//拍照后的图片剪裁
            if(null != data){
                mImagePath = data.getStringExtra("imagefile");
                Log.d("MainActivity",mImagePath);
                imagePicker.getImageLoader().displayImage(MainActivity.this, mImagePath, mImage, mWidth, mWidth);
            }
        }
    }
}
