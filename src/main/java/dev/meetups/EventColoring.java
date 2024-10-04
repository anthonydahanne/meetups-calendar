package dev.meetups;

public class EventColoring {

	public static String colorFromGroupName(String groupName) {
		return switch(groupName.toLowerCase()) {
			case String g when g.contains("js") -> "steelblue";
			case String g when g.contains("javascript") -> "steelblue";
			case String g when g.contains("jug") -> "orangered";
			case String g when g.contains("java") -> "orangered";
			case String g when g.contains("typescript") -> "royalblue";
			case String g when g.contains("flutter") -> "lightskyblue";
			case String g when g.contains("snowflake") -> "deepskyblue";
			case String g when g.contains("cpp") -> "slateblue";
			case String g when g.contains("snowflake") -> "deepskyblue";
			case String g when g.contains("aws") -> "orange";
			case String g when g.contains("rb") -> "red";
			case String g when g.contains("ruby") -> "ruby";
			case String g when g.contains("ai") -> "darkslateblue";
			case String g when g.contains("python") -> "limegreen";
			case String g when g.contains("py") -> "limegreen";
			case String g when g.contains("linux") -> "sandybrown";
			case String g when g.contains("unity") -> "slategrey";
			case String g when g.contains("laravel") -> "darkorchid";
			case String g when g.contains("php") -> "darkorchid";
			case String g when g.contains("craft") -> "plum";
			default -> "grey";
		};
	}

}
