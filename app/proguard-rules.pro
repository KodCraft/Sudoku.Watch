# Wear OS Sudoku ProGuard Rules
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.sudoku.watch.game.** { *; }
