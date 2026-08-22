package com.fooddelivery.gui;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;

import java.nio.file.Path;

public class GuiDataLoader {
    private final Path dataDirectory;

    public GuiDataLoader() {
        this(Path.of("data"));
    }

    public GuiDataLoader(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("Data directory must not be null");
        }
        this.dataDirectory = dataDirectory;
    }

    public DatasetLoadResult load() {
        return new DatasetLoader().load(dataDirectory);
    }
}
