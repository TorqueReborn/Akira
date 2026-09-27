import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../theme/app_colors.dart';

/// TV Input Field supporting D-Pad navigation, OK to edit, and right-arrow focus to toggle password visibility.
class TvInputField extends StatefulWidget {
  final String label;
  final String hintText;
  final IconData prefixIcon;
  final TextEditingController? controller;
  final TextInputType keyboardType;
  final bool isPassword;
  final FocusNode? focusNode;
  final ValueChanged<String>? onSubmitted;
  final ValueChanged<String>? onChanged;
  final FocusNode? nextFocusNode;
  final FocusNode? previousFocusNode;

  const TvInputField({
    super.key,
    required this.label,
    required this.hintText,
    required this.prefixIcon,
    this.controller,
    this.keyboardType = TextInputType.text,
    this.isPassword = false,
    this.focusNode,
    this.onSubmitted,
    this.onChanged,
    this.nextFocusNode,
    this.previousFocusNode,
  });

  @override
  State<TvInputField> createState() => _TvInputFieldState();
}

class _TvInputFieldState extends State<TvInputField> {
  late final FocusNode _cardFocusNode;
  final FocusNode _textInputFocusNode = FocusNode();
  final FocusNode _visibilityIconFocusNode = FocusNode();

  bool _isCardFocused = false;
  bool _isEditing = false;
  bool _isVisibilityFocused = false;
  bool _obscureText = true;

  FocusNode get _effectiveCardFocusNode => widget.focusNode ?? _cardFocusNode;

  @override
  void initState() {
    super.initState();
    _cardFocusNode = FocusNode();
    _obscureText = widget.isPassword;

    _effectiveCardFocusNode.addListener(_onCardFocusChange);
    _textInputFocusNode.addListener(_onTextInputFocusChange);
    _visibilityIconFocusNode.addListener(_onVisibilityFocusChange);
  }

  void _onCardFocusChange() {
    if (mounted) {
      setState(() {
        _isCardFocused = _effectiveCardFocusNode.hasFocus;
      });
    }
  }

  void _onTextInputFocusChange() {
    if (mounted) {
      setState(() {
        _isEditing = _textInputFocusNode.hasFocus;
      });
    }
  }

  void _onVisibilityFocusChange() {
    if (mounted) {
      setState(() {
        _isVisibilityFocused = _visibilityIconFocusNode.hasFocus;
      });
    }
  }

  void _startEditing() {
    _textInputFocusNode.requestFocus();
  }

  void _toggleObscureText() {
    setState(() {
      _obscureText = !_obscureText;
    });
  }

  @override
  void dispose() {
    _effectiveCardFocusNode.removeListener(_onCardFocusChange);
    _textInputFocusNode.removeListener(_onTextInputFocusChange);
    _visibilityIconFocusNode.removeListener(_onVisibilityFocusChange);
    _cardFocusNode.dispose();
    _textInputFocusNode.dispose();
    _visibilityIconFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isObscured = widget.isPassword && _obscureText;
    final isFieldHighlighted = _isCardFocused || _isEditing;

    return Focus(
      focusNode: _effectiveCardFocusNode,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          // Pressing OK / Center / Enter activates the on-screen keyboard
          if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter ||
              event.logicalKey == LogicalKeyboardKey.space ||
              event.logicalKey == LogicalKeyboardKey.gameButtonA) {
            _startEditing();
            return KeyEventResult.handled;
          }
          // D-Pad Right -> focus password visibility toggle button if password field
          if (event.logicalKey == LogicalKeyboardKey.arrowRight && widget.isPassword) {
            _visibilityIconFocusNode.requestFocus();
            return KeyEventResult.handled;
          }
          // D-Pad Down -> next widget
          if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
            if (widget.nextFocusNode != null) {
              widget.nextFocusNode!.requestFocus();
              return KeyEventResult.handled;
            }
          }
          // D-Pad Up -> previous widget
          if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
            if (widget.previousFocusNode != null) {
              widget.previousFocusNode!.requestFocus();
              return KeyEventResult.handled;
            }
          }
        }
        return KeyEventResult.ignored;
      },
      child: GestureDetector(
        onTap: _startEditing,
        child: AnimatedScale(
          scale: isFieldHighlighted ? 1.02 : 1.0,
          duration: const Duration(milliseconds: 180),
          curve: Curves.easeOutCubic,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.only(left: 4, bottom: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      widget.label,
                      style: TextStyle(
                        color: isFieldHighlighted
                            ? AppColors.primary
                            : AppColors.textSecondary,
                        fontSize: 14,
                        fontWeight:
                            isFieldHighlighted ? FontWeight.w700 : FontWeight.w500,
                        letterSpacing: 0.3,
                      ),
                    ),
                    if (isFieldHighlighted && !_isEditing)
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withAlpha(25),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          widget.isPassword ? 'Press OK to edit  •  → to toggle' : 'Press OK to edit',
                          style: const TextStyle(
                            color: AppColors.primary,
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
              AnimatedContainer(
                duration: const Duration(milliseconds: 180),
                curve: Curves.easeOutCubic,
                decoration: BoxDecoration(
                  color: isFieldHighlighted ? Colors.white : const Color(0xFFF8FAFC),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: isFieldHighlighted
                        ? AppColors.primary
                        : const Color(0xFFE2E8F0),
                    width: isFieldHighlighted ? 2.5 : 1.2,
                  ),
                ),
                child: Focus(
                  onKeyEvent: (node, event) {
                    if (event is KeyDownEvent) {
                      if (event.logicalKey == LogicalKeyboardKey.escape) {
                        _effectiveCardFocusNode.requestFocus();
                        return KeyEventResult.handled;
                      }
                    }
                    return KeyEventResult.ignored;
                  },
                  child: TextField(
                    controller: widget.controller,
                    focusNode: _textInputFocusNode,
                    keyboardType: widget.keyboardType,
                    obscureText: isObscured,
                    onChanged: widget.onChanged,
                    onSubmitted: (value) {
                      if (widget.nextFocusNode != null) {
                        widget.nextFocusNode!.requestFocus();
                      } else {
                        _effectiveCardFocusNode.requestFocus();
                      }
                      widget.onSubmitted?.call(value);
                    },
                    textInputAction: widget.nextFocusNode != null
                        ? TextInputAction.next
                        : TextInputAction.done,
                    style: const TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                    cursorColor: AppColors.primary,
                    decoration: InputDecoration(
                      hintText: widget.hintText,
                      hintStyle: const TextStyle(
                        color: Color(0xFF94A3B8),
                        fontSize: 15,
                        fontWeight: FontWeight.w400,
                      ),
                      prefixIcon: Icon(
                        widget.prefixIcon,
                        color: isFieldHighlighted
                            ? AppColors.primary
                            : const Color(0xFF64748B),
                        size: 24,
                      ),
                      suffixIcon: widget.isPassword
                          ? Padding(
                              padding: const EdgeInsets.only(right: 6),
                              child: Focus(
                                focusNode: _visibilityIconFocusNode,
                                onKeyEvent: (node, event) {
                                  if (event is KeyDownEvent) {
                                    // OK / Select / Space toggles password visibility
                                    if (event.logicalKey == LogicalKeyboardKey.select ||
                                        event.logicalKey == LogicalKeyboardKey.enter ||
                                        event.logicalKey == LogicalKeyboardKey.space ||
                                        event.logicalKey == LogicalKeyboardKey.gameButtonA) {
                                      _toggleObscureText();
                                      return KeyEventResult.handled;
                                    }
                                    // Left arrow -> move focus back to input field card
                                    if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
                                      _effectiveCardFocusNode.requestFocus();
                                      return KeyEventResult.handled;
                                    }
                                    // Down arrow -> move to next widget (e.g. Sign In button)
                                    if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                                      if (widget.nextFocusNode != null) {
                                        widget.nextFocusNode!.requestFocus();
                                        return KeyEventResult.handled;
                                      }
                                    }
                                    // Up arrow -> move to previous widget (e.g. Email field)
                                    if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
                                      if (widget.previousFocusNode != null) {
                                        widget.previousFocusNode!.requestFocus();
                                        return KeyEventResult.handled;
                                      }
                                    }
                                  }
                                  return KeyEventResult.ignored;
                                },
                                child: AnimatedScale(
                                  scale: _isVisibilityFocused ? 1.15 : 1.0,
                                  duration: const Duration(milliseconds: 150),
                                  child: Material(
                                    color: Colors.transparent,
                                    child: InkWell(
                                      onTap: _toggleObscureText,
                                      borderRadius: BorderRadius.circular(10),
                                      child: Padding(
                                        padding: const EdgeInsets.all(8.0),
                                        child: Icon(
                                          _obscureText
                                              ? Icons.visibility_off_outlined
                                              : Icons.visibility_outlined,
                                          color: _isVisibilityFocused
                                              ? AppColors.primary
                                              : (isFieldHighlighted
                                                  ? const Color(0xFF64748B)
                                                  : const Color(0xFF94A3B8)),
                                          size: 22,
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            )
                          : null,
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 20,
                        vertical: 18,
                      ),
                      border: InputBorder.none,
                      focusedBorder: InputBorder.none,
                      enabledBorder: InputBorder.none,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
