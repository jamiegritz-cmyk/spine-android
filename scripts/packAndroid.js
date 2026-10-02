import fs from 'fs';
import path from 'path';
import JSZip from 'jszip';

async function packDirectory(dirPath, zipFolder) {
  const entries = fs.readdirSync(dirPath, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dirPath, entry.name);
    if (entry.isDirectory()) {
      const subFolder = zipFolder.folder(entry.name);
      await packDirectory(fullPath, subFolder);
    } else {
      const content = fs.readFileSync(fullPath);
      zipFolder.file(entry.name, content);
    }
  }
}

async function main() {
  const zip = new JSZip();
  const androidDir = path.resolve('android');
  const publicDir = path.resolve('public');
  if (!fs.existsSync(publicDir)) {
    fs.mkdirSync(publicDir, { recursive: true });
  }

  console.log('Packing android folder into Spine-Android-Release.zip...');
  const rootFolder = zip.folder('Spine-Android');
  await packDirectory(androidDir, rootFolder);

  // Add README
  rootFolder.file(
    'README.md',
    `# SPINE - Native Android CD Music Player for Pixel 9

100% Native Android Project written with Kotlin, Jetpack Compose, Media3 (ExoPlayer), Room, and MediaStore.

## How to Install and Run on Google Pixel 9:

### Option A: Via Android Studio (Recommended)
1. Unzip this file onto your computer.
2. Open Android Studio (Ladybug, Koala, or newer).
3. Select **File > Open** and choose the unzipped \`Spine-Android\` folder.
4. Let Gradle sync dependencies automatically.
5. Connect your Pixel 9 via USB (with **Developer Options > USB Debugging** enabled) or via Wi-Fi debugging.
6. Click the green **Run (▶)** button in Android Studio.
7. The app will compile, install directly onto your Pixel 9, and launch the native SPINE music player!

### Option B: Build APK with Command Line
\`\`\`bash
cd Spine-Android
./gradlew assembleDebug
\`\`\`
The compiled APK will be generated at:
\`app/build/outputs/apk/debug/app-debug.apk\`

Install onto your Pixel 9 using ADB:
\`\`\`bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
\`\`\`
`
  );

  const buffer = await zip.generateAsync({
    type: 'nodebuffer',
    compression: 'DEFLATE',
    compressionOptions: { level: 9 }
  });

  const targetPath = path.join(publicDir, 'Spine-Android-Release.zip');
  fs.writeFileSync(targetPath, buffer);
  console.log('Successfully generated:', targetPath, `(${buffer.length} bytes)`);
}

main().catch(console.error);
