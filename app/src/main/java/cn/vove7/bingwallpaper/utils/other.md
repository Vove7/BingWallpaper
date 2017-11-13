
打开图片

{
    File file=new File();
    LogHelper.logD(null,file.getPath());
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
       intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
       uri = FileProvider.getUriForFile(this,
               "cn.vove7.bingwallpaper.fileprovider", file);
    } else {
       uri = Uri.parse("file://" + file.getPath());
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.setDataAndType(uri, "image/*");
    startActivity(intent);
}