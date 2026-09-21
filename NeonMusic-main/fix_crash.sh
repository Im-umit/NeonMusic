#!/bin/bash
​echo "1/3: Eksik XML kaynak klasörleri ve tema dosyaları oluşturuluyor..."
mkdir -p app/src/main/res/values
​1. Themes XML (Uygulama Teması)
​cat << 'EOF' > app/src/main/res/values/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
<style name="Theme.NeonMusic" parent="android:Theme.Material.NoTitleBar">
<item name="android:statusBarColor">#0A0A0C</item>
<item name="android:navigationBarColor">#0A0A0C</item>
</style>
</resources>
EOF
​2. Strings XML
​cat << 'EOF' > app/src/main/res/values/strings.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
<string name="app_name">NeonMusic</string>
</resources>
EOF
​echo "2/3: Git senkronizasyonu yapılıyor..."
git add .
git commit -m "Eksik XML tema ve kaynak dosyalari eklendi (Cokme duzeltildi)"
git push origin main
​echo "3/3: Bitti! GitHub Actions yeni APK'yi derlemeye basladi."
EOF
