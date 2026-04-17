package com.example.btl_dnc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.btl_dnc.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    private String initialTimeFilter = "";
    private List<String> initialCategoryFilters = new ArrayList<>();
    private List<String> initialStatusFilters = new ArrayList<>();


    public void setInitialFilters(String time, List<String> categories, List<String> statuses) {
        this.initialTimeFilter = time != null ? time : "";
        if (categories != null) this.initialCategoryFilters = new ArrayList<>(categories);
        if (statuses != null) this.initialStatusFilters = new ArrayList<>(statuses);
    }

    // 1. Cập nhật Interface để nhận thêm List Status
    public interface OnFilterAppliedListener {
        void onFilterApplied(String timeFilter, List<String> selectedCategories, List<String> selectedStatuses);
    }

    private OnFilterAppliedListener listener;

    public void setFilterListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_filter_bottom_sheet, container, false);

        Button btnApply = view.findViewById(R.id.btnApply);
        RadioGroup rgTime = view.findViewById(R.id.rgTime);

        ChipGroup cgCategories = view.findViewById(R.id.cgCategories);
        TextView tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);

        ChipGroup cgStatus = view.findViewById(R.id.cgStatus);
        TextView tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        // 1. Khôi phục Khoảng thời gian
        if ("Hôm nay".equals(initialTimeFilter)) rgTime.check(R.id.rbToday);
        else if ("Tuần này".equals(initialTimeFilter)) rgTime.check(R.id.rbThisWeek);
        else if ("Tháng này".equals(initialTimeFilter)) rgTime.check(R.id.rbThisMonth);

        // 2. Khôi phục Danh mục
        for (int i = 0; i < cgCategories.getChildCount(); i++) {
            Chip chip = (Chip) cgCategories.getChildAt(i);
            if (initialCategoryFilters.contains(chip.getText().toString())) {
                chip.setChecked(true); // Tích lại nếu đã từng chọn
            }
        }
        tvCategoryTitle.setText("Danh mục ( " + initialCategoryFilters.size() + " )");

        // 3. Khôi phục Trạng thái
        for (int i = 0; i < cgStatus.getChildCount(); i++) {
            Chip chip = (Chip) cgStatus.getChildAt(i);
            if (initialStatusFilters.contains(chip.getText().toString())) {
                chip.setChecked(true); // Tích lại nếu đã từng chọn
            }
        }
        tvStatusTitle.setText("Trạng thái ( " + initialStatusFilters.size() + " )");

        // 2. Logic tự động đếm số lượng Chip Danh Mục được chọn
        cgCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            tvCategoryTitle.setText("Danh mục ( " + checkedIds.size() + " )");
        });

        // 3. Logic tự động đếm số lượng Chip Trạng Thái được chọn
        cgStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            tvStatusTitle.setText("Trạng thái ( " + checkedIds.size() + " )");
        });

        // Nút Xóa: Reset toàn bộ
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            rgTime.clearCheck();
            cgCategories.clearCheck();
            cgStatus.clearCheck();
        });

        // Nút Xong: Lấy dữ liệu và gửi đi
        btnApply.setOnClickListener(v -> {
            // Lấy thời gian (Cho phép rỗng nếu không chọn)
            int checkedTimeId = rgTime.getCheckedRadioButtonId();
            String timeFilter = ""; // Mặc định là rỗng (Bỏ qua)
            if (checkedTimeId == R.id.rbToday) timeFilter = "Hôm nay";
            else if (checkedTimeId == R.id.rbThisWeek) timeFilter = "Tuần này";
            else if (checkedTimeId == R.id.rbThisMonth) timeFilter = "Tháng này";

            // Lấy danh mục
            List<String> categories = new ArrayList<>();
            for (int id : cgCategories.getCheckedChipIds()) {
                Chip chip = view.findViewById(id);
                categories.add(chip.getText().toString());
            }

            // Lấy trạng thái
            List<String> statuses = new ArrayList<>();
            for (int id : cgStatus.getCheckedChipIds()) {
                Chip chip = view.findViewById(id);
                statuses.add(chip.getText().toString());
            }

            // Truyền tất cả về Fragment
            if (listener != null) {
                listener.onFilterApplied(timeFilter, categories, statuses);
            }
            dismiss();
        });

        return view;
    }
    // Làm cho các góc trên của Popup bo tròn
    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }
}