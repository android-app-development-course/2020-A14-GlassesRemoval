package com.example.camera_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView imgview ;
    private Button btn_image;
    private  Button btn_save;
    private Button btn_take;
    private Button btn_choice;
    private Button btn_lemon;
    private Button btn_back;
    private ImageView imgroll;
    private  Button btn_old;
    private final int RESULT_CODE = 1;
    private final int SAVE_CODE = 2;
    private Bitmap bitmap_orgin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request_permissions();


        setContentView(R.layout.activity_main);
        imgroll =  findViewById(R.id.imageroll);
        Bitmap bitmap_roll = getBitmap(imgroll.getDrawable());
        bitmap_roll = circleBitmapByShader(bitmap_roll,100,100);
        imgroll.setImageBitmap(bitmap_roll);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.img_animation);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);
        imgroll.startAnimation(animation);

        imgview = findViewById(R.id.imageview);
        btn_image = findViewById(R.id.btn_img);
        btn_save = findViewById(R.id.btn_save);
        btn_take = findViewById(R.id.take_photo);
        btn_choice = findViewById(R.id.choose_from_album);
        btn_lemon = findViewById(R.id.btn_lemon);
        btn_old = findViewById(R.id.btn_old);
        btn_back = findViewById(R.id.btn_back);

        final Drawable drawable = imgview.getDrawable();
        bitmap_orgin=getBitmap(imgview.getDrawable());


        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"正在绘制，请稍等...", Toast.LENGTH_SHORT).show();
                Bitmap new_bitmap = testGaussBlur(getBitmap(imgview.getDrawable()), 7, 10);
                imgview.setImageBitmap(new_bitmap);

            }
        });

        imgroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//用来打开相机的Intent
                startActivityForResult(takePhotoIntent, RESULT_CODE);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = getBitmap(imgview.getDrawable());
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "myPhoto", "");

//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                Toast.makeText(MainActivity.this,"保存成功！", Toast.LENGTH_SHORT).show();
            }
        });

        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//用来打开相机的Intent
                startActivityForResult(takePhotoIntent, RESULT_CODE);

            }
        });

        btn_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, SAVE_CODE);

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imgview.setImageBitmap(bitmap_orgin);
            }
        });

        btn_old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap new_bitmap = testBitmap(getBitmap(imgview.getDrawable()));
                imgview.setImageBitmap(new_bitmap);
            }
        });

        btn_lemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap new_bitmap =Lomo1 (getBitmap(imgview.getDrawable()));
                imgview.setImageBitmap(new_bitmap);
            }
        });



    }

    // 请求多个权限
    private void request_permissions() {
        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
        List<String> permissionList = new ArrayList<>();

        // 判断权限是否已经授予，没有就把该权限添加到列表中
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }



        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionList.toArray(new String[permissionList.size()]), 1002);
        } else {
//            Toast.makeText(this, "多个权限你都有了，不用再次申请", Toast.LENGTH_LONG).show();
        }
    }

    // 请求权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1002:
                // 1002请求码对应的是申请多个权限
                if (grantResults.length > 0) {
                    // 因为是多个权限，所以需要一个循环获取每个权限的获取情况
                    for (int i = 0; i < grantResults.length; i++) {
                        // PERMISSION_DENIED 这个值代表是没有授权，我们可以把被拒绝授权的权限显示出来
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                            Toast.makeText(MainActivity.this, permissions[i] + "权限被拒绝了", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_CODE:
                if (resultCode == Activity.RESULT_OK){
                    Bundle bundle = data.getExtras();
                    // 转化数据
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    // 将图片添加到imagineview中
                    imgview.setImageBitmap(bitmap);
                    bitmap_orgin = getBitmap(imgview.getDrawable());
                }

                break;
            case SAVE_CODE:
                if (resultCode == RESULT_OK) {
                    // 加载本地库的图片
                    Uri uri = data.getData();
                    imgview.setImageURI(uri);
                    bitmap_orgin = getBitmap(imgview.getDrawable());
                }
                break;
            default:
                break;
        }
    }



    //从imageview中获取bitmap图片
    private Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //去色
    public static int[] discolor(Bitmap bitmap) {

        int picHeight = bitmap.getHeight();
        int picWidth = bitmap.getWidth();

        int[] pixels = new int[picWidth * picHeight];
        bitmap.getPixels(pixels, 0, picWidth, 0, 0, picWidth, picHeight);

        for (int i = 0; i < picHeight; ++i) {
            for (int j = 0; j < picWidth; ++j) {
                int index = i * picWidth + j;
                int color = pixels[index];
                int r = (color & 0x00ff0000) >> 16;
                int g = (color & 0x0000ff00) >> 8;
                int b = (color & 0x000000ff);
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                pixels[index] = grey << 16 | grey << 8 | grey | 0xff000000;
            }
        }

        return pixels;

    }

        //反相
    public static int[] reverseColor(int[] pixels) {

        int length = pixels.length;
        int[] result = new int[length];
        for (int i = 0; i < length; ++i) {
            int color = pixels[i];
            int r = 255 - (color & 0x00ff0000) >> 16;
            int g = 255 - (color & 0x0000ff00) >> 8;
            int b = 255 - (color & 0x000000ff);
            result[i] = r << 16 | g << 8 | b | 0xff000000;
        }
        return result;

    }

    //高斯模糊
    public static void gaussBlur(int[] data, int width, int height, int radius,
                                 float sigma) {
        float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
        float pb = -1.0f / (2 * sigma * sigma);
        // generate the Gauss Matrix
        float[] gaussMatrix = new float[radius * 2 + 1];
        float gaussSum = 0f;
        for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
            float g = (float) (pa * Math.exp(pb * x * x));
            gaussMatrix[i] = g;
            gaussSum += g;
        }

        for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
            gaussMatrix[i] /= gaussSum;
        }

        // x direction
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = x + j;
                    if (k >= 0 && k < width) {
                        int index = y * width + k;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }
                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }

        // y direction
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = y + j;
                    if (k >= 0 && k < height) {
                        int index = k * width + x;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }

                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }
    }

   // 颜色减淡
    public static void colorDodge(int[] baseColor, int[] mixColor) {

        for (int i = 0, length = baseColor.length; i < length; ++i) {
            int bColor = baseColor[i];
            int br = (bColor & 0x00ff0000) >> 16;
            int bg = (bColor & 0x0000ff00) >> 8;
            int bb = (bColor & 0x000000ff);

            int mColor = mixColor[i];
            int mr = (mColor & 0x00ff0000) >> 16;
            int mg = (mColor & 0x0000ff00) >> 8;
            int mb = (mColor & 0x000000ff);

            int nr = colorDodgeFormular(br, mr);
            int ng = colorDodgeFormular(bg, mg);
            int nb = colorDodgeFormular(bb, mb);

            baseColor[i] = nr << 16 | ng << 8 | nb | 0xff000000;
        }

    }

    private static int colorDodgeFormular(int base, int mix) {

        int result = base + (base * mix) / (255 - mix);
        result = result > 255 ? 255 : result;
        return result;

    }
    //灰度图对应到rgb空间后r、g、b的值
    public static int[] simpleReverseColor(int[] pixels) {
        int length = pixels.length;
        int[] result = new int[length];
        for (int i = 0; i < length; ++i) {
            int color = pixels[i];
            int b = 255 - (color & 0x000000ff);
            result[i] = b << 16 | b << 8 | b | 0xff000000;
        }
        return result;
    }

    //组合，返回素描图
    public static Bitmap testGaussBlur(Bitmap src, int r, int fai) {

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = discolor(src);
        int[] copixels = simpleReverseColor(pixels);
        gaussBlur(copixels, width, height, r, fai);
        colorDodge(pixels, copixels);
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height,
                Bitmap.Config.RGB_565);
        return bitmap;

    }


    public static Bitmap testBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        float[] array = {1,0,0,0,50,
                0,1,0,0,50,
                0,0,1,0,0,
                0,0,0,1,0};
        cm.set(array);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        canvas.drawBitmap(bitmap, 0, 0, paint);
        return output;
    }
    /**
     * 利用BitmapShader绘制底部圆角图片
     *
     * @param bitmap
     *              待处理图片
     * @param edgeWidth
     *              正方形控件大小
     * @param radius
     *              圆角半径大小
     * @return
     *              结果图片
     */
    private Bitmap circleBitmapByShader(Bitmap bitmap, int edgeWidth, int radius) {
        if(bitmap == null) {
            throw new NullPointerException("Bitmap can't be null");
        }

        float btWidth = bitmap.getWidth();
        float btHeight = bitmap.getHeight();
        // 水平方向开始裁剪的位置
        float btWidthCutSite = 0;
        // 竖直方向开始裁剪的位置
        float btHeightCutSite = 0;
        // 裁剪成正方形图片的边长，未拉伸缩放
        float squareWidth = 0f;
        if(btWidth > btHeight) { // 如果矩形宽度大于高度
            btWidthCutSite = (btWidth - btHeight) / 2f;
            squareWidth = btHeight;
        } else { // 如果矩形宽度不大于高度
            btHeightCutSite = (btHeight - btWidth) / 2f;
            squareWidth = btWidth;
        }

        // 设置拉伸缩放比
        float scale = edgeWidth * 1.0f / squareWidth;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        // 将矩形图片裁剪成正方形并拉伸缩放到控件大小
        Bitmap squareBt = Bitmap.createBitmap(bitmap, (int)btWidthCutSite, (int)btHeightCutSite, (int)squareWidth, (int)squareWidth, matrix, true);

        // 初始化绘制纹理图
        BitmapShader bitmapShader = new BitmapShader(squareBt, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // 初始化目标bitmap
        Bitmap targetBitmap = Bitmap.createBitmap(edgeWidth, edgeWidth, Bitmap.Config.ARGB_8888);

        // 初始化目标画布
        Canvas targetCanvas = new Canvas(targetBitmap);

        // 初始化画笔
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);

        // 利用画笔绘制圆形图
        targetCanvas.drawRoundRect(new RectF(0, 0, edgeWidth, edgeWidth), radius, radius, paint);

        return targetBitmap;
    }

    public static Bitmap Lomo1(Bitmap bitmap){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        int color = 0;
        int a,r,g,b,r1,g1,b1;

        int[] oldPx = new int[w * h];
        int[] newPx = new int[w * h];

        bitmap.getPixels(oldPx, 0, w, 0, 0, w, h);
        for(int i = 0; i < w * h; i++){
            color = oldPx[i];

            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);

            r1 = (int) (1.7 * r + 0.1 * g + 0.1 * b - 73.1);
            g1 = (int) (0 * r + 1.7 * g + 0.1 * b - 73.1);
            b1 = (int) (0 * r + 0.1 * g + 1.6 * b - 73.1);

//检查各通道值是否超出范围
            if(r1 > 255){
                r1 = 255;
            }

            if(g1 > 255){
                g1 = 255;
            }

            if(b1 > 255){
                b1 = 255;
            }

            newPx[i] = Color.argb(a, r1, g1, b1);
        }
        resultBitmap.setPixels(newPx, 0, w, 0, 0, w, h);

        return resultBitmap;
    }

}




