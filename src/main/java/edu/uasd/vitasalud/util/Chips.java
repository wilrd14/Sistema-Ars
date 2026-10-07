package edu.uasd.vitasalud.util;

import edu.uasd.vitasalud.model.ConChip;
import javafx.scene.control.Label;

/** Crea chips de estado con el estilo de {@code vitasalud.css} (.chip + .chip-aprobada, etc.). */
public final class Chips {

    private Chips() {
    }

    public static Label crear(ConChip estado) {
        Label chip = new Label();
        aplicar(chip, estado);
        return chip;
    }

    /** Útil dentro de {@code TableCell.updateItem}: reutiliza el mismo Label al reciclar celdas. */
    public static void aplicar(Label chip, ConChip estado) {
        chip.getStyleClass().removeIf(c -> c.startsWith("chip"));
        chip.setText(estado.etiqueta());
        chip.getStyleClass().addAll("chip", estado.claseChip());
    }
}
