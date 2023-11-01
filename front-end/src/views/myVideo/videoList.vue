<template>
    <v-card elevation="0">
        <v-table>
            <thead>
                <tr>
                    <th class="text-left">
                        封面
                    </th>
                    <th class="text-left">
                        标题
                    </th>
                    <th class="text-left">
                        上传日期
                    </th>
                    <th class="text-left">
                        时长
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in desserts" :key="item.name">
                    <td>{{ item.name }}</td>
                    <td>{{ item.calories }}</td>
                </tr>
            </tbody>
        </v-table>
    </v-card>
</template>
<script setup>
</script>