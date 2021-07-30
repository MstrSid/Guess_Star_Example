package by.kos.guessstarexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import by.kos.guessstarexample.databinding.ActivityMainBinding;
import by.kos.guessstarexample.util.DownloadContentTask;
import by.kos.guessstarexample.util.DownloadImageTask;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private final String url = "https://www.forbes.ru/rating/403469-40-samyh-uspeshnyh-zvezd-rossii-do-40-let-reyting-forbes";
    private ArrayList<String> imagesUrls;
    private ArrayList<String> names;
    private int numberOfQuestion;
    private int numberOfRightAnswer;
    private ArrayList<Button> buttons;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setupButtonsArray();
        imagesUrls = new ArrayList<>();
        names = new ArrayList<>();
        getContent();
        playGame();

        binding.btnAnswer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer(v);
            }
        });

        binding.btnAnswer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer(v);
            }
        });

        binding.btnAnswer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer(v);
            }
        });

        binding.btnAnswer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAnswer(v);
            }
        });
    }

    private void setupButtonsArray() {
        buttons = new ArrayList<>();
        buttons.add(binding.btnAnswer1);
        buttons.add(binding.btnAnswer2);
        buttons.add(binding.btnAnswer3);
        buttons.add(binding.btnAnswer4);
    }

    private void playGame() {
        generateQuestion();
        DownloadImageTask task = new DownloadImageTask();
        try {
            Bitmap bitmap = task.execute(imagesUrls.get(numberOfQuestion)).get();
            if (bitmap != null) {
                binding.ivStar.setImageBitmap(bitmap);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfRightAnswer) {
                        buttons.get(i).setText(names.get(numberOfQuestion));
                    } else {
                        int wrongAnswer = generateWrongAnswers();
                        if(wrongAnswer != numberOfRightAnswer)
                            buttons.get(i).setText(names.get(wrongAnswer));
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int generateWrongAnswers() {
        return (int) (Math.random() * names.size());
    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    private void onClickAnswer(View v) {
        if (toast != null) {
            toast.cancel();
        }
        Button button = (Button) v;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfRightAnswer) {
            toast = Toast.makeText(getApplicationContext(), "Right!", Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(getApplicationContext(), "Wrong! Right is: " + names.get(numberOfQuestion), Toast.LENGTH_LONG);
        }
        toast.show();
        playGame();
    }

    private void getContent() {
        DownloadContentTask contentTask = new DownloadContentTask();
        try {
            String content = contentTask.execute(url).get();
            String start = "<div class=\"items\">";
            String finish = "<div class=\"panel-pane pane-rating-content\">";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);
            String splitContent = "";

            while (matcher.find()) {
                splitContent = matcher.group(1);
            }

            Pattern patternImage = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("title=\"(.*?)\"");
            Matcher matcherImage = null;
            Matcher matcherName = null;

            if (splitContent != null) {
                matcherImage = patternImage.matcher(splitContent);
                matcherName = patternName.matcher(splitContent);
            }

            if (matcherImage != null) {
                while (matcherImage.find()) {
                    imagesUrls.add(matcherImage.group(1));
                }
            }

            if (matcherName != null) {
                while (matcherName.find()) {
                    names.add(matcherName.group(1));
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}