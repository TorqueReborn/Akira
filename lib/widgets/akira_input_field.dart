import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Reusable Modern Light-Mode Input Field with animated focus glow and subtle elevation.
class AkiraInputField extends StatefulWidget {
  final String hintText;
  final IconData prefixIcon;
  final TextEditingController? controller;
  final TextInputType keyboardType;
  final bool isPassword;
  final ValueChanged<String>? onChanged;

  const AkiraInputField({
    super.key,
    required this.hintText,
    required this.prefixIcon,
    this.controller,
    this.keyboardType = TextInputType.text,
    this.isPassword = false,
    this.onChanged,
  });

  @override
  State<AkiraInputField> createState() => _AkiraInputFieldState();
}

class _AkiraInputFieldState extends State<AkiraInputField> {
  late final FocusNode _focusNode;
  bool _isFocused = false;
  bool _obscureText = true;

  @override
  void initState() {
    super.initState();
    _focusNode = FocusNode();
    _obscureText = widget.isPassword;
    _focusNode.addListener(_onFocusChange);
  }

  void _onFocusChange() {
    setState(() {
      _isFocused = _focusNode.hasFocus;
    });
  }

  @override
  void dispose() {
    _focusNode.removeListener(_onFocusChange);
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isObscured = widget.isPassword && _obscureText;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      curve: Curves.easeInOut,
      decoration: BoxDecoration(
        color: _isFocused ? Colors.white : const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: _isFocused ? AppColors.primary : const Color(0xFFE2E8F0),
          width: _isFocused ? 1.5 : 1.0,
        ),
        boxShadow: [
          if (_isFocused)
            BoxShadow(
              color: AppColors.primary.withAlpha(41), // ~0.16 opacity
              blurRadius: 14,
              spreadRadius: 1,
              offset: const Offset(0, 4),
            )
          else
            BoxShadow(
              color: const Color(0xFF0F172A).withAlpha(8), // ~0.03 opacity
              blurRadius: 6,
              spreadRadius: 0,
              offset: const Offset(0, 2),
            ),
        ],
      ),
      child: TextField(
        controller: widget.controller,
        focusNode: _focusNode,
        keyboardType: widget.keyboardType,
        obscureText: isObscured,
        onChanged: widget.onChanged,
        style: const TextStyle(
          color: AppColors.textPrimary,
          fontSize: 15,
          fontWeight: FontWeight.w500,
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
            color: _isFocused ? AppColors.primary : const Color(0xFF64748B),
            size: 22,
          ),
          suffixIcon: widget.isPassword
              ? IconButton(
                  icon: Icon(
                    _obscureText
                        ? Icons.visibility_off_outlined
                        : Icons.visibility_outlined,
                    color: _isFocused
                        ? AppColors.primary
                        : const Color(0xFF64748B),
                    size: 22,
                  ),
                  onPressed: () {
                    setState(() {
                      _obscureText = !_obscureText;
                    });
                  },
                )
              : null,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 20,
            vertical: 18,
          ),
          border: InputBorder.none,
          focusedBorder: InputBorder.none,
          enabledBorder: InputBorder.none,
          errorBorder: InputBorder.none,
          disabledBorder: InputBorder.none,
        ),
      ),
    );
  }
}
