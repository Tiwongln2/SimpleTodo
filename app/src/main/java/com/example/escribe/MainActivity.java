package com.example.escribe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 50;

    List<String> items;

    Button addButton;
    EditText edItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.Add);
        edItem = findViewById(R.id.editItem);
        rvItems = findViewById(R.id.listItems);

        loadItems();

        itemsAdapter = new ItemsAdapter(items);

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                items.remove(position);
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void OnItemClicked(int position) {
                Log.e("MainActivity", "Single click at position " + position);
                // Create the new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // Passes new list item to activity
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // Displays activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };
        ItemsAdapter itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem =  edItem.getText().toString();
                // Add item to the model
                items.add(todoItem);
                // Notification that item has been added
                itemsAdapter.notifyItemInserted(items.size() - 1);
                edItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    // Handles editing activity updates
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_TEXT_CODE && resultCode == RESULT_OK) {
            // Retrieve updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // Obtain original position of the edited item from the position key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);
            // Update the model at the right position with new text
            items.set(position, itemText);
            // Notify adapter
            itemsAdapter.notifyItemChanged(position);
            // Persist changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call tot onActivityResult");
        }
    }

    private File getDataFile() {
        // Returns a file under directory and called data.txt
        return new File(getFilesDir(), "data.txt");
    }

    /*
    *** This function will load items by reading every line of the dta file
     */
    private void loadItems() {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    /*
    *** This function saves items by writing them into the data file
     */
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}
