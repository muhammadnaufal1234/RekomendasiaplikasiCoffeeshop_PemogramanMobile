Add-Type -TypeDefinition "using System;
using System.Drawing;
using System.Drawing.Imaging;

public class ImageProcessorSafeZeroCrop {
    public static void ProcessImage(string srcPath, string destPath, int tolerance) {
        Bitmap srcBmp = new Bitmap(srcPath);
        // Assuming top-left corner is the background color
        Color bgColor = srcBmp.GetPixel(0, 0); 
        
        Bitmap destBmp = new Bitmap(srcBmp.Width, srcBmp.Height, PixelFormat.Format32bppArgb);
        
        for (int y = 0; y < srcBmp.Height; y++) {
            for (int x = 0; x < srcBmp.Width; x++) {
                Color p = srcBmp.GetPixel(x, y);
                // Check color distance from the background color
                int diff = Math.Abs(p.R - bgColor.R) + Math.Abs(p.G - bgColor.G) + Math.Abs(p.B - bgColor.B);
                
                // If it's pure white/cream or very close to the background color, make it transparent
                if ((p.R > 230 && p.G > 230 && p.B > 220) || (diff < tolerance && p.A > 0)) {
                    destBmp.SetPixel(x, y, Color.Transparent);
                } else {
                    // This keeps all anti-aliased edge pixels perfectly intact!
                    destBmp.SetPixel(x, y, p); 
                }
            }
        }
        
        srcBmp.Dispose();
        destBmp.Save(destPath, ImageFormat.Png);
        destBmp.Dispose();
    }
}" -ReferencedAssemblies System.Drawing
$srcPath = 'c:\Users\ASUS\AndroidStudioProjects\monokromcoffee2\app\src\main\res\drawable\logo_terbarucoffee.png'
$tempPath = 'c:\Users\ASUS\AndroidStudioProjects\monokromcoffee2\app\src\main\res\drawable\logo_terbarucoffee_tmp.png'

if (Test-Path $srcPath) {
    [ImageProcessorSafeZeroCrop]::ProcessImage($srcPath, $tempPath, 80)
    Move-Item -Path $tempPath -Destination $srcPath -Force
    Write-Host "Berhasil! Background dihapus TANPA crop sama sekali!"
} else {
    Write-Host "File tidak ditemukan!"
}
