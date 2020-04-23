package usu.pajak.fariz.model;

import java.math.BigDecimal;

public class UserPajakTax {
    private BigDecimal reminder;
    private Integer index;
    private BigDecimal reminder_kegiatan;
    private Integer index_kegiatan;
    private BigDecimal reminder_jasmed;
    private Integer index_jasmed;

    public BigDecimal getReminder_kegiatan() {
        return reminder_kegiatan;
    }

    public void setIndex_kegiatan(Integer index_kegiatan) {
        this.index_kegiatan = index_kegiatan;
    }

    public Integer getIndex_kegiatan() {
        return index_kegiatan;
    }

    public void setReminder_kegiatan(BigDecimal reminder_kegiatan) {
        this.reminder_kegiatan = reminder_kegiatan;
    }

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
