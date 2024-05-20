package com.example.demo1;

import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MenuManager {
    private List<TextView> menuItems;
    private TextView currentSelectedMenuItem;

    public MenuManager() {
        menuItems = new ArrayList<>();
    }

    public void addMenuItem(TextView menuItem) {
        menuItems.add(menuItem);
        menuItem.setOnClickListener(v -> {
            setSelectedMenuItem(menuItem);
        });
    }

    private void setSelectedMenuItem(TextView menuItem) {
        if (currentSelectedMenuItem != null) {
            currentSelectedMenuItem.setSelected(false);
        }
        menuItem.setSelected(true);
        currentSelectedMenuItem = menuItem;
    }
}
