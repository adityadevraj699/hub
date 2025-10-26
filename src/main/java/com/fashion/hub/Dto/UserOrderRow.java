package com.fashion.hub.Dto;

public class UserOrderRow {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private long pending;
    private long processing;
    private long shipped;
    private long delivered;
    private long cancelled;

    public UserOrderRow(Long id, String name, String email, String phone,
                        long pending, long processing, long shipped,
                        long delivered, long cancelled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pending = pending;
        this.processing = processing;
        this.shipped = shipped;
        this.delivered = delivered;
        this.cancelled = cancelled;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public long getPending() { return pending; }
    public long getProcessing() { return processing; }
    public long getShipped() { return shipped; }
    public long getDelivered() { return delivered; }
    public long getCancelled() { return cancelled; }
}
