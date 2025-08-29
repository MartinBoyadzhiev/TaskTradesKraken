package common;

import common.dto.BookUpdate;

public interface BookHandler {
    void handleUpdateData(BookUpdate data);
    LocalOrderBook getBook();
}
