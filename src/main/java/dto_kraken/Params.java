package dto_kraken;

import java.util.List;

public record Params(String channel, List<String> symbol, int depth, boolean snapshot) {
}
