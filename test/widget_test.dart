import 'package:akira/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('Akira Login Screen UI smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const AkiraApp());

    // Verify Akira branding elements
    expect(find.text('AKIRA'), findsOneWidget);
    expect(find.text('Your gateway to the anime world.'), findsOneWidget);

    // Verify Email and Password input fields exist
    expect(find.text('Email'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);

    // Verify LOGIN button exists
    expect(find.text('LOGIN'), findsOneWidget);
  });
}
