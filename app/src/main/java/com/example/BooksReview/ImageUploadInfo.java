package com.example.BooksReview;

public class ImageUploadInfo {

    String judul;
    String deskripsi;
    String gambar;
    //String search;


    public ImageUploadInfo() {


    }

    public ImageUploadInfo(String judul, String deskripsi, String gambar) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.gambar = gambar;

    }

    public String getJudul() {
        return judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getGambar() {
        return gambar;
    }

}
