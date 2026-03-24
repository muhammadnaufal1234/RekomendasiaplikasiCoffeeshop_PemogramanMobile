import sys
from PIL import Image

def remove_background(image_path):
    try:
        img = Image.open(image_path).convert("RGBA")
        datas = img.getdata()
        
        # Get background color from the top-left corner
        bg_color = datas[0]
        
        newData = []
        for item in datas:
            # If the pixel is close to the background color, make it transparent
            # Calculate distance
            diff = sum(abs(item[i] - bg_color[i]) for i in range(3))
            if diff < 50: # Tolerance
                newData.append((item[0], item[1], item[2], 0))
            else:
                newData.append(item)
                
        img.putdata(newData)
        img.save(image_path, "PNG")
        print("Success")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        remove_background(sys.argv[1])
    else:
        print("Please provide image path")
