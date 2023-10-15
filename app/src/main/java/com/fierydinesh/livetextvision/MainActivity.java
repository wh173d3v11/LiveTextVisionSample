package com.fierydinesh.livetextvision;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap sampleImage;
    private SparseArray<TextBlock> textBlocks;

    // Variables to track text selection
    private float startX;
    private float startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.img);

        TextView ocrButton = findViewById(R.id.ocrButton);

        sampleImage = BitmapFactory.decodeResource(getResources(), R.drawable.sample_vision);
        imageView.setImageBitmap(sampleImage);

        // Initialize textBlocks
        textBlocks = new SparseArray<>();

        // Set a touch listener on the ImageView
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Convert the view coordinates to image coordinates
                Matrix inverse = new Matrix();
                imageView.getImageMatrix().invert(inverse);
                float[] touchPoint = {motionEvent.getX(), motionEvent.getY()};
                inverse.mapPoints(touchPoint);

                startX = touchPoint[0];
                startY = touchPoint[1];

                Log.d("Coordinates", "X " + startX + " Y " + startY + " height=" + view.getHeight());
                highlightLineContainingTouchPoint(startX, startY);
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTextRecognition();
    }

    private void startTextRecognition() {
        // Initialize the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            // Handle cases where the text recognizer is not operational
            // This may require downloading additional data or checking device settings
            // You can display an error message or prompt the user to enable it
            return;
        }

        // Create a Frame from the loaded image
        Frame frame = new Frame.Builder().setBitmap(sampleImage).build();

        // Perform text recognition
        textBlocks = textRecognizer.detect(frame);
    }

    private void highlightLineContainingTouchPoint(float x, float y) {
        // Initialize a new Bitmap for highlighting
        Bitmap highlightedImage = sampleImage.copy(sampleImage.getConfig(), true);
        Canvas canvas = new Canvas(highlightedImage);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAlpha(128);

        // Iterate over textBlocks to find the line containing the touch point
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);
            for (Text line : textBlock.getComponents()) {
                // Check if the touch point is within the bounding box of the line
                if (x >= line.getBoundingBox().left &&
                        x <= line.getBoundingBox().right &&
                        y >= line.getBoundingBox().top &&
                        y <= line.getBoundingBox().bottom) {
                    float left = line.getBoundingBox().left;
                    float top = line.getBoundingBox().top;
                    float bottom = line.getBoundingBox().bottom;
                    float right = line.getBoundingBox().right;
                    Log.e("Coordinates", "Line top" + top + "Bottom" + bottom);

                    // Highlight the line containing the touch point
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }

        // Update the ImageView with the highlighted image
        imageView.setImageBitmap(highlightedImage);
    }

}