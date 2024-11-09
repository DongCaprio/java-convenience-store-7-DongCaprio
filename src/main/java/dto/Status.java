package dto;

import exception.Exception;

public enum Status {

    Y("Y"),
    N("N");

    private String status;

    private Exception exception = new Exception();

    Status(String status) {
        this.status = status;
    }

    public static Status checkStatusInput(String input) {
        if ("Y".equalsIgnoreCase(input)) {
            return Status.Y;
        }
        return Status.N;
    }

}