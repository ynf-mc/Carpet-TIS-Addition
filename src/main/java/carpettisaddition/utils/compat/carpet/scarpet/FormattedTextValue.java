package carpettisaddition.utils.compat.carpet.scarpet;

import carpet.script.value.NullValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import carpettisaddition.utils.Messenger;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * Copied from fabric-carpet 1.16
 * For less merge effort to 1.14 branch
 */
public class FormattedTextValue extends StringValue
{
	Text text;
	public FormattedTextValue(Text text)
	{
		super(null);
		this.text = text;
	}

	public static Value combine(Value left, Value right) {
		BaseText text;
		if (left instanceof FormattedTextValue)
		{
			text = Messenger.copy((BaseText)((FormattedTextValue) left).getText());
		}
		else
		{
			if (left instanceof NullValue)
				return right;
			text = Messenger.s(left.getString());
		}

		if (right instanceof FormattedTextValue)
		{
			text.append(Messenger.copy((BaseText)((FormattedTextValue) right).getText()));
			return new FormattedTextValue(text);
		}
		else
		{
			if (right instanceof NullValue)
				return left;
			text.append(right.getString());
			return new FormattedTextValue(text);
		}
	}

	@Override
	public String getString() {
		return text.getString();
	}

	@Override
	public boolean getBoolean() {
		  return text.getSiblings().size() > 0;
	}

	@Override
	public Value clone()
	{
		return new FormattedTextValue(text);
	}

	@Override
	public String getTypeString()
	{
		return "text";
	}

	public Text getText()
	{
		return text;
	}

	public Tag toTag(boolean force)
	{
		if (!force) throw new RuntimeException(String.valueOf(this));
		String s = Text.Serializer.toJson(text);

		//#if MC >= 11500
		return StringTag.of(s);
		//#else
		//$$ return new StringTag(s);
		//#endif
	}

	@Override
	public Value add(Value o) {
		return combine(this, o);
	}

	public String serialize()
	{
		return Text.Serializer.toJson(text);
	}

	public static FormattedTextValue deserialize(String serialized)
	{
		return new FormattedTextValue(Text.Serializer.fromJson(serialized));
	}
}