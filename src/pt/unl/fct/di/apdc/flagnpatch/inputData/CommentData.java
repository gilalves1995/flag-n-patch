package pt.unl.fct.di.apdc.flagnpatch.inputData;

public class CommentData {

    public String content;

    public CommentData() {
    }

    public CommentData(String content) {
        this.content = content;
    }

    public boolean valid() {
        return validContent();
    }

    private boolean validContent() {
        return this.content != null && !this.content.equals("");
    }

}
