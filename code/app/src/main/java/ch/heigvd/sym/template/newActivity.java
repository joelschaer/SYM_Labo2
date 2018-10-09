package ch.heigvd.sym.template;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;


public class newActivity extends Activity {

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("myResult", getResources().getString(R.string.back));
        // Activity finished ok, return the data
        setResult(newActivity.RESULT_OK, data);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        //Field from MainActivity
        Intent intent = getIntent();
        String password = intent.getStringExtra("passwordGiven");
        String email = intent.getStringExtra("emailEntered");

        //Field from layout
        TextView emailView = findViewById(R.id.email);
        TextView passwordView = findViewById(R.id.password);
        final TextView iemiView = findViewById(R.id.iEMI);
        final ImageView iv = findViewById(R.id.imageView);

        //Setting textView
        emailView.setText(email);
        passwordView.setText(password);

        // Test needed permission with the Dexter library
        Dexter.withActivity(newActivity.this)
                .withPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {

                    // if permission are accepted
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        List<PermissionGrantedResponse> acceptPerms = report.getGrantedPermissionResponses();
                        for (PermissionGrantedResponse perm : acceptPerms) {
                            if (perm.getPermissionName().equals(Manifest.permission.READ_PHONE_STATE)) {
                                // Access to Phone state so get the IEMI
                                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                                iemiView.setText(telephonyManager.getDeviceId());
                            }
                                // Access to External Storage to get the image
                            if (perm.getPermissionName().equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "miguel.png");
                                iv.setImageBitmap(bmp);
                            }
                        }
                    }

                    // ASK for permissions
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

    }

}
