package com.example.currencypricedatacollector;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_BASE_URL = "https://api.coingecko.com/api/v3/"; // api base uri

    private TextView priceTextView;
    private TextView countdownTextView;
    private Timer timer;
    private int elapsedTimeSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        priceTextView = findViewById(R.id.priceTextView);
        countdownTextView = findViewById(R.id.countdownTextView);

        // Start a timer
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currencyPrice();
            }
        }, 0, 3600000); // Every 1 hour  will run

        // Schedule a task to stop the timer after 24 hours
        new CountDownTimer(24 * 3600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                elapsedTimeSeconds += 1;
                updateCountdownTimer();
            }

            @Override
            public void onFinish() {
                timer.cancel();
                timer.purge();
            }
        }.start();
    }

    private void updateCountdownTimer() {
        int remainingSeconds = 3600 - elapsedTimeSeconds;
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        String countdownText = String.format("Next update in: %02d:%02d:%02d", hours, minutes, seconds);
        countdownTextView.setText(countdownText);
    }

    private void currencyPrice() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CoinGeckoService service = retrofit.create(CoinGeckoService.class);
        Call<Object> call = service.getCryptoPrice("USD", "btc");
        // get price of 1 btc in dollar
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Object responseData = response.body();

                    // Check if the response can be treated as JSON
                    if (responseData instanceof Map) {
                        Map<?, ?> responseMap = (Map<?, ?>) responseData;
                        Object usdData = responseMap.get("usd");

                        if (usdData instanceof Map) {
                            Map<?, ?> usdDataMap = (Map<?, ?>) usdData;
                            Object btcPrice = usdDataMap.get("btc");

                            if (btcPrice instanceof Number) {
                                double price = ((Number) btcPrice).doubleValue();


                                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

                                String priceText = timestamp+":"+"\n"+"BTC Price in USD: " + price;

                                priceTextView.setText(priceText);
                            } else {
                                priceTextView.setText("Unexpected BTC price format.");
                            }
                        } else {
                            priceTextView.setText("Unexpected usd data format.");
                        }
                    } else {
                        priceTextView.setText("Unexpected response structure.");
                    }
                } else {
                    priceTextView.setText("Failed to fetch data. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                priceTextView.setText("Error fetching data: " + t.getMessage());
            }
        });
    }
}
