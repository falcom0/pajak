package usu.pajak.fariz.model;

import java.math.BigDecimal;

public class UserPajakTax {
    private BigDecimal reminder;//blm
    private Integer index;//blm
    private BigDecimal reminder_jasmed;
    private Integer index_jasmed;

    public Integer getIndex() {
        return index;
    }

    public Integer getIndex_jasmed() {
        return index_jasmed;
    }

    public BigDecimal getReminder() {
        return reminder;
    }

    public BigDecimal getReminder_jasmed() {
        return reminder_jasmed;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setIndex_jasmed(Integer index_jasmed) {
        this.index_jasmed = index_jasmed;
    }

    public void setReminder(BigDecimal reminder) {
        this.reminder = reminder;
    }

    public void setReminder_jasmed(BigDecimal reminder_jasmed) {
        this.reminder_jasmed = reminder_jasmed;
    }
}
