package com.local.mytv;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private ArrayList<String> channelNames = new ArrayList<>();
    private ArrayList<String> channelUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayerView playerView = findViewById(R.id.player_view);
        ListView listView = findViewById(R.id.list_view);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        loadLocalChannels();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, channelNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String url = channelUrls.get(position);
            playUrl(url);
        });
    }

    private void loadLocalChannels() {
        try {
            AssetManager assets = getAssets();
            InputStream is = assets.open("channels.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            String currentName = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#EXTINF:-1")) {
                    int nameStart = line.indexOf("tvg-name=\"") + 10;
                    int nameEnd = line.indexOf("\"", nameStart);
                    if (nameStart > 9 && nameEnd > nameStart) {
                        currentName = line.substring(nameStart, nameEnd);
                    }
                } else if (line.startsWith("http://") || line.startsWith("https://")) {
                    if (currentName != null) {
                        channelNames.add(currentName);
                        channelUrls.add(line);
                        currentName = null;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playUrl(String url) {
        player.stop();
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
