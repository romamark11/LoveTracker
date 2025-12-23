package com.example.lovtracker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.Gravity;
import android.content.SharedPreferences;
import java.util.Locale;

public class MainActivity extends Activity {
    private SharedPreferences sharedPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = getSharedPreferences("LoveTracker", MODE_PRIVATE);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        mainLayout.setBackgroundColor(0xFFF5F5F5);
        
        TextView title = new TextView(this);
        title.setText("LoveTracker");
        title.setTextSize(28f);
        title.setTextColor(0xFFE91E63);
        title.setPadding(0, 0, 0, 24);
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(title);
        
        mainLayout.addView(createLabel("Часы общения сегодня:"));
        EditText hoursInput = new EditText(this);
        hoursInput.setHint("например: 5.5");
        hoursInput.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        hoursInput.setPadding(16, 16, 16, 16);
        hoursInput.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(hoursInput);
        
        mainLayout.addView(createLabel("Качество общения (1-10):"));
        EditText qualityInput = new EditText(this);
        qualityInput.setHint("от 1 до 10");
        qualityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        qualityInput.setPadding(16, 16, 16, 16);
        qualityInput.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(qualityInput);
        
        mainLayout.addView(createLabel("Что делали вместе:"));
        EditText activityInput = new EditText(this);
        activityInput.setHint("Смотрели фильм, готовили...");
        activityInput.setPadding(16, 16, 16, 16);
        activityInput.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(activityInput);
        
        mainLayout.addView(createLabel("Эмоции через запятую:"));
        EditText emotionsInput = new EditText(this);
        emotionsInput.setHint("радость, спокойствие, благодарность");
        emotionsInput.setPadding(16, 16, 16, 16);
        emotionsInput.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(emotionsInput);
        
        Button analyzeButton = new Button(this);
        analyzeButton.setText("АНАЛИЗИРОВАТЬ ДЕНЬ");
        analyzeButton.setTextColor(0xFFFFFFFF);
        analyzeButton.setBackgroundColor(0xFFE91E63);
        analyzeButton.setPadding(16, 16, 16, 16);
        mainLayout.addView(analyzeButton);
        
        TextView resultText = new TextView(this);
        resultText.setText("Результат появится здесь");
        resultText.setTextSize(18f);
        resultText.setPadding(0, 24, 0, 0);
        resultText.setGravity(Gravity.CENTER);
        mainLayout.addView(resultText);
        
        loadSavedData(hoursInput, qualityInput, activityInput, emotionsInput, resultText);
        
        analyzeButton.setOnClickListener(v -> {
            float hours = 0f;
            try { hours = Float.parseFloat(hoursInput.getText().toString()); } catch (Exception e) { hours = 0f; }
            int quality = 5;
            try { quality = Integer.parseInt(qualityInput.getText().toString()); } catch (Exception e) { quality = 5; }
            String emotions = emotionsInput.getText().toString();
            
            float score = calculateScore(hours, quality, emotions);
            String advice = generateAdvice(hours, quality, score);
            
            resultText.setText(String.format(Locale.getDefault(), "Близость: %.1f%%\n\n%s", score, advice));
            saveData(hours, quality, activityInput.getText().toString(), emotions);
        });
        
        setContentView(mainLayout);
    }
    
    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16f);
        label.setTextColor(0xFF333333);
        label.setPadding(0, 16, 0, 8);
        return label;
    }
    
    public float calculateScore(float hours, int quality, String emotions) {
        float score = 50f;
        if (hours >= 3f && hours <= 6f) score += 20f;
        else if (hours > 6f) score += 15f;
        else if (hours > 1f) score += 10f;
        else score -= 10f;
        
        score += (quality - 5) * 3f;
        
        String[] positive = {"радость", "любовь", "спокойствие", "счастье", "благодарность"};
        String[] negative = {"грусть", "злость", "обида", "тревога", "раздражение"};
        String emo = emotions.toLowerCase();
        for (String w : positive) if (emo.contains(w)) score += 5f;
        for (String w : negative) if (emo.contains(w)) score -= 3f;
        
        return Math.max(0f, Math.min(100f, score));
    }
    
    public String generateAdvice(float hours, int quality, float score) {
        if (hours < 1) return "💡 Мало общались. Планируйте время вместе!";
        if (quality < 4) return "💡 Качество низкое. Попробуйте активное слушание.";
        if (score < 30) return "⚠️ Обсудите чувства открыто!";
        if (score > 80) return "🌟 Отличный день! Запомните, что сблизило.";
        return "💡 Обычный день. Маленькие жесты важны.";
    }
    
    private void saveData(float hours, int quality, String activity, String emotions) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat("hours", hours);
        editor.putInt("quality", quality);
        editor.putString("activity", activity);
        editor.putString("emotions", emotions);
        editor.apply();
    }
    
    private void loadSavedData(EditText hours, EditText quality, EditText activity, EditText emotions, TextView result) {
        hours.setText(String.valueOf(sharedPrefs.getFloat("hours", 0f)));
        quality.setText(String.valueOf(sharedPrefs.getInt("quality", 5)));
        activity.setText(sharedPrefs.getString("activity", ""));
        emotions.setText(sharedPrefs.getString("emotions", ""));
        
        float savedScore = calculateScore(
            sharedPrefs.getFloat("hours", 0f),
            sharedPrefs.getInt("quality", 5),
            sharedPrefs.getString("emotions", "")
        );
        result.setText(String.format(Locale.getDefault(), "Последний результат: %.1f%%", savedScore));
    }
}
