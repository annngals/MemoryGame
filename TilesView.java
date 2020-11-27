package com.example.memorine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class Card {
    Paint p = new Paint();
    int color; //цвет карты
    int solveColor = Color.rgb(255, 255, 255), // цвет фона
            backColor = Color.rgb(201, 173, 167); // цвет рубашки
    boolean isOpen = false, isSolved = false;
    float x, y, width, height;
    int i, j;
    int offset = 10;


    public Card(int i, int j, int color) {
        this.color = color;
        this.i = i;
        this.j = j;
    }

    public void setTransform(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Canvas c) { // нарисовать карту в виде цветного прямоугольника
        if (isSolved) {
            p.setColor(solveColor);
        } else {
            if (isOpen) {
                p.setColor(color);
            } else
                p.setColor(backColor);
        }
        c.drawRect(x + offset, y + offset, x + width - offset, y + height - offset, p);
    }

    public boolean flip(float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = !isOpen;
            return true;
        }
        return false;
    }

}

public class TilesView extends View {

    int col = 3, row = 4; // размеры сетки

    final int PAUSE_LENGTH = 2; // пауза для запоминания карт в секундах
    boolean isOnPauseNow = false;

    int openedID1, openedID2; //первая открытая карта

    int openedCard = 0;  // число открытых карт
    int solvedCards = 0; // чисто угаданных карт

    ArrayList<Card> cards = new ArrayList<>(); //карты

    //цвета карт
    ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(
            Color.rgb(39, 125, 161),
            Color.rgb(67, 170, 139),
            Color.rgb(77, 144, 142),
            Color.rgb(144, 190, 109),
            Color.rgb(243, 114, 44),
            Color.rgb(249, 199, 79)
    ));


    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context);

        int counter = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {

                int color;
                if (counter >= row * col / 2) {
                    int rnd = new Random().nextInt(colors.size());
                    color = colors.get(rnd);
                    colors.remove(rnd);

                } else {
                    color = colors.get(counter);
                }
                cards.add(new Card(i, j, color));
                counter++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = canvas.getHeight() / row;
        int width = canvas.getWidth() / col;

        for (Card c : cards) {
            int left = c.j * width;
            int top = c.i * height;

            c.setTransform(left, top, width, height);
            c.draw(canvas);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();


        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow) {

            for (Card c : cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {

                        openedCard++;
                        openedID1 = c.i * col + c.j;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {

                    if (c.flip(x, y)) {
                        openedCard++;
                        openedID2 = c.i * col + c.j;

                        Log.d("pair_",
                                "first=" + openedID1 + "   second=" + openedID2);
                        if (cards.get(openedID1).color == c.color) {

                            cards.get(openedID1).isSolved = true;
                            c.isSolved = true;
                            solvedCards += 2;

                            if (solvedCards == row * col) {
                                Toast toast = Toast.makeText(getContext(),
                                        "Hey, you won", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                        invalidate();
                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;
                        return true;
                    }
                }


            }

        }
        return true;
    }

    public void newGame() {
        //  запуск новой игры
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {
            }
            Log.d("mytag", "Pause finished");
            return null;
        }

        // после паузы, перевернуть все карты обратно


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c : cards) {
                if (c.isOpen) {
                    c.isOpen = false;

                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}